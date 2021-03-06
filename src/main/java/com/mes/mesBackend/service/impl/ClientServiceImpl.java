package com.mes.mesBackend.service.impl;

import com.mes.mesBackend.dto.request.ClientRequest;
import com.mes.mesBackend.dto.response.ClientResponse;
import com.mes.mesBackend.entity.*;
import com.mes.mesBackend.exception.BadRequestException;
import com.mes.mesBackend.exception.NotFoundException;
import com.mes.mesBackend.helper.impl.S3UploaderImpl;
import com.mes.mesBackend.mapper.ModelMapper;
import com.mes.mesBackend.repository.ClientRepository;
import com.mes.mesBackend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.mes.mesBackend.entity.enumeration.InspectionType.NONE;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final ClientTypeService clientTypeService;
    private final CountryCodeService countryCodeService;
    private final S3UploaderImpl s3Service;
    private final ModelMapper modelMapper;
    private final CurrencyService currencyService;
    private final PayTypeService payTypeService;

    public Client getClientOrThrow(Long id) throws NotFoundException {
        return clientRepository.findByIdAndDeleteYnFalse(id)
                .orElseThrow(() -> new NotFoundException("client does not exist. input client id: " + id));
    }

    // 거래처 생성
    public ClientResponse createClient(ClientRequest clientRequest) throws NotFoundException {
        CountryCode countryCode = clientRequest.getCountryCode() != null ? countryCodeService.getCountryCodeOrThrow(clientRequest.getCountryCode()) : null;
        ClientType clientType = clientTypeService.getClientTypeOrThrow(clientRequest.getClientType());
        Currency currency = clientRequest.getCurrencyUnit() != null ? currencyService.getCurrencyOrThrow(clientRequest.getCurrencyUnit()) : null;
        PayType payType = clientRequest.getPaymentMethod() != null ? payTypeService.getPayTypeOrThrow(clientRequest.getPaymentMethod()) : null;
        Client client = modelMapper.toEntity(clientRequest, Client.class);

        if(clientRequest.getInspectionType().equals(NONE)) client.setInspectionType(null);

        client.addJoin(countryCode, clientType, currency, payType);
        clientRepository.save(client);
        return modelMapper.toResponse(client, ClientResponse.class);
    }

    // 거래처 조회
    public ClientResponse getClient(Long id) throws NotFoundException {
        Client client = getClientOrThrow(id);
        return modelMapper.toResponse(client, ClientResponse.class);
    }

    // 거래처 조건 페이징 조회 (거래처 유형, 거래처 코드, 거래처 명)
    public List<ClientResponse> getClients(
            Long type,
            String code,
            String clientName
    ) {
        List<Client> clients = clientRepository.findByTypeAndCodeAndName(type, code, clientName);
        return modelMapper.toListResponses(clients, ClientResponse.class);
    }

    // 거래처 수정
    public ClientResponse updateClient(Long id, ClientRequest clientRequest) throws NotFoundException {
        Client newClient = modelMapper.toEntity(clientRequest, Client.class);
        Client findClient = getClientOrThrow(id);

        CountryCode newCountryCode = clientRequest.getCountryCode() != null ? countryCodeService.getCountryCodeOrThrow(clientRequest.getCountryCode()) : null;
        ClientType newClientType = clientTypeService.getClientTypeOrThrow(clientRequest.getClientType());
        Currency currency = clientRequest.getCurrencyUnit() != null ? currencyService.getCurrencyOrThrow(clientRequest.getCurrencyUnit()) : null;
        PayType payType = clientRequest.getPaymentMethod() != null ? payTypeService.getPayTypeOrThrow(clientRequest.getPaymentMethod()) : null;

        findClient.put(newClient, newCountryCode, newClientType, currency, payType);
        clientRepository.save(findClient);
        return modelMapper.toResponse(findClient, ClientResponse.class);
    }

    // 사업자 등록증 파일 업로드
    // client/거래처 명/파일명(날싸시간)
    public ClientResponse createBusinessFileToClient(Long id, MultipartFile businessFile) throws IOException, NotFoundException, BadRequestException {
        Client client = getClientOrThrow(id);
        String fileName = "client/" + client.getClientCode() + "/";
        client.setBusinessFile(s3Service.upload(businessFile, fileName));
        clientRepository.save(client);
        return modelMapper.toResponse(client, ClientResponse.class);
    }

    // 거래처 삭제
    public void deleteClient(Long id) throws NotFoundException {
        Client client = getClientOrThrow(id);
        client.delete();
        clientRepository.save(client);
    }

    // 사업자 등록증 파일 삭제 (aws 권한 문제로 안됨)
    private void deleteBusinessFileToClient(Long id) throws IOException, NotFoundException {
        Client client = getClientOrThrow(id);
        s3Service.delete(client.getBusinessFile());
        client.setBusinessFile(null);
    }
}
