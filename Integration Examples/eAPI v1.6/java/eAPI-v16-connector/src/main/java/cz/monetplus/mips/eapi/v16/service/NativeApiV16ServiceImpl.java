package cz.monetplus.mips.eapi.v16.service;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;

import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cz.monetplus.mips.eapi.v16.connector.INativeAPIv16Resource;
import cz.monetplus.mips.eapi.v16.connector.entity.CustReq;
import cz.monetplus.mips.eapi.v16.connector.entity.CustRes;
import cz.monetplus.mips.eapi.v16.connector.entity.EchoReq;
import cz.monetplus.mips.eapi.v16.connector.entity.EchoRes;
import cz.monetplus.mips.eapi.v16.connector.entity.PayInitReq;
import cz.monetplus.mips.eapi.v16.connector.entity.PayOneclickInitReq;
import cz.monetplus.mips.eapi.v16.connector.entity.PayRefundReq;
import cz.monetplus.mips.eapi.v16.connector.entity.PayReq;
import cz.monetplus.mips.eapi.v16.connector.entity.PayRes;
import cz.monetplus.mips.eapi.v16.connector.entity.ext.Extension;


@Service
public class NativeApiV16ServiceImpl implements NativeApiV16Service  {

	static final Logger LOG = LoggerFactory.getLogger(NativeApiV16ServiceImpl.class);

	private INativeAPIv16Resource nativeApiV16Resource;
	
	@Value("${merchant.id}")
	private String merchantId;
	
	@Autowired
	private CryptoService cryptoService;
		
    @Autowired
	public NativeApiV16ServiceImpl(INativeAPIv16Resource nativeApiV1Resource) {
		this.nativeApiV16Resource = nativeApiV1Resource;
	}
    
	@Override
    public PayRes paymentInit(File initFile) throws MipsException {
		try {
			Gson gson = new GsonBuilder().create();
			PayInitReq req = gson.fromJson(FileUtils.readFileToString(initFile, "UTF-8"), PayInitReq.class);
			req.merchantId = merchantId;
			cryptoService.createSignature(req);
			Response response = nativeApiV16Resource.paymentInit(req);
			if (response == null || response.getStatus() != 200) {
				throw new MipsException(RespCode.INTERNAL_ERROR, "No response from nativeAPI for paymentInit operation, http response " + (response != null ? response.getStatus() : "--")); 
			}
			PayRes res = response.readEntity(PayRes.class);
			if (!cryptoService.isSignatureValid(res)) {
				throw new MipsException(RespCode.INTERNAL_ERROR, "Invalid signature for response " + res.toJson()); 
			}
			return res;
		} 
		catch (Exception e) {
			throw new MipsException(RespCode.INTERNAL_ERROR, "nativeAPI call for paymentInit operation failed: ", e); 
		}
    }

    
	@Override
	public String paymentProcess(String payId) throws MipsException {
		try {
			PayReq req = new PayReq(merchantId, payId);
			cryptoService.createSignature(req);
			Response response = nativeApiV16Resource.paymentProcess(req.merchantId, req.payId, req.dttm, URLEncoder.encode(req.signature, "UTF-8"));
			if (response == null || response.getStatus() != 303) {
				throw new MipsException(RespCode.INTERNAL_ERROR, "Expected 303 http response from nativeAPI for paymentProcess operation, got response " 
						+ (response != null ? response.getStatus() : "--") + ", payment not found or expired?"); 
			}
			URI uri = response.getLocation();
			if (uri == null) {
				throw new MipsException(RespCode.INTERNAL_ERROR, "Missing location header in response of paymentProcess operation"); 
			}
			return uri.toString();
		} 
		catch (Exception e) {
			throw new MipsException(RespCode.INTERNAL_ERROR, "nativeAPI call for paymentProcess operation failed: ", e); 
		}
	}

	@Override
	public PayRes paymentStatus(String payId) throws MipsException {
		try {
			PayReq req = new PayReq(merchantId, payId);
			cryptoService.createSignature(req);
			Response response = nativeApiV16Resource.paymentStatus(req.merchantId, req.payId, req.dttm, URLEncoder.encode(req.signature, "UTF-8"));
			if (response == null || response.getStatus() != 200) {
				throw new MipsException(RespCode.INTERNAL_ERROR, "No response from nativeAPI for paymentStatus operation, http response " + (response != null ? response.getStatus() : "--")); 
			}
			PayRes res = response.readEntity(PayRes.class);
			if (!cryptoService.isSignatureValid(res)) {
				throw new MipsException(RespCode.INTERNAL_ERROR, "Invalid signature for response " + res.toJson()); 
			}
			if (res.extensions != null) {
				for (Extension ext : res.extensions) {
					if (!cryptoService.isSignatureValid(ext)) {
						throw new MipsException(RespCode.INTERNAL_ERROR, "Invalid signature for response extension " + ext.toJson()); 
					}
				}
			}
			return res;
		} 
		catch (Exception e) {
			throw new MipsException(RespCode.INTERNAL_ERROR, "nativeAPI call for paymentStatus operation failed: ", e); 
		}
	}

	@Override
	public PayRes paymentClose(String payId) throws MipsException {
		try {
			PayReq req = new PayReq(merchantId, payId);
			cryptoService.createSignature(req);
			Response response = nativeApiV16Resource.paymentClose(req);
			if (response == null || response.getStatus() != 200) {
				throw new MipsException(RespCode.INTERNAL_ERROR, "No response from nativeAPI for paymentClose operation, http response " + (response != null ? response.getStatus() : "--")); 
			}
			PayRes res = response.readEntity(PayRes.class);
			if (!cryptoService.isSignatureValid(res)) {
				throw new MipsException(RespCode.INTERNAL_ERROR, "Invalid signature for response " + res.toJson()); 
			}
			return res;
		} 
		catch (Exception e) {
			throw new MipsException(RespCode.INTERNAL_ERROR, "nativeAPI call for paymentClose operation failed: ", e); 
		}
	}

	@Override
	public PayRes paymentReverse(String payId) throws MipsException {
		try {
			PayReq req = new PayReq(merchantId, payId);
			cryptoService.createSignature(req);
			Response response = nativeApiV16Resource.paymentReverse(req);
			if (response == null || response.getStatus() != 200) {
				throw new MipsException(RespCode.INTERNAL_ERROR, "No response from nativeAPI for paymentReverse operation, http response " + (response != null ? response.getStatus() : "--")); 
			}
			PayRes res = response.readEntity(PayRes.class);
			if (!cryptoService.isSignatureValid(res)) {
				throw new MipsException(RespCode.INTERNAL_ERROR, "Invalid signature for response " + res.toJson()); 
			}
			return res;
		} 
		catch (Exception e) {
			throw new MipsException(RespCode.INTERNAL_ERROR, "nativeAPI call for paymentReverse operation failed: ", e); 
		}
	}

	
	@Override
	public PayRes paymentRefund(String payId, Long amount) throws MipsException {
		try {
			PayRefundReq req = new PayRefundReq(merchantId, payId, amount);
			cryptoService.createSignature(req);
			Response response = nativeApiV16Resource.paymentRefund(req);
			if (response == null || response.getStatus() != 200) {
				throw new MipsException(RespCode.INTERNAL_ERROR, "No response from nativeAPI for paymentRefund operation, http response " + (response != null ? response.getStatus() : "--")); 
			}
			PayRes res = response.readEntity(PayRes.class);
			if (!cryptoService.isSignatureValid(res)) {
				throw new MipsException(RespCode.INTERNAL_ERROR, "Invalid signature for response " + res.toJson()); 
			}
			return res;
		} 
		catch (Exception e) {
			throw new MipsException(RespCode.INTERNAL_ERROR, "nativeAPI call for paymentRefund operation failed: ", e); 
		}
	}

	
	@Override
	public EchoRes echoGet() throws MipsException {
		try {
			EchoReq req = new EchoReq(merchantId);
			cryptoService.createSignature(req);
			Response response = nativeApiV16Resource.echo(req.merchantId, req.dttm, URLEncoder.encode(req.signature, "UTF-8"));
			if (response == null || response.getStatus() != 200) {
				throw new MipsException(RespCode.INTERNAL_ERROR, "No response from nativeAPI for echo (GET) operation, http response " + (response != null ? response.getStatus() : "--")); 
			}
			EchoRes res = response.readEntity(EchoRes.class);
			if (!cryptoService.isSignatureValid(res)) {
				throw new MipsException(RespCode.INTERNAL_ERROR, "Invalid signature for response " + res.toJson()); 
			}
			return res;
		} 
		catch (Exception e) {
			throw new MipsException(RespCode.INTERNAL_ERROR, "nativeAPI call for echo (GET) operation failed: ", e); 
		}
	}

	@Override
	public EchoRes echoPost() throws MipsException {
		try {
			EchoReq req = new EchoReq(merchantId);
			cryptoService.createSignature(req);
			Response response = nativeApiV16Resource.echo(req);
			if (response == null || response.getStatus() != 200) {
				throw new MipsException(RespCode.INTERNAL_ERROR, "No response from nativeAPI for echo (POST) operation, http response " + (response != null ? response.getStatus() : "--")); 
			}
			EchoRes res = response.readEntity(EchoRes.class);
			if (!cryptoService.isSignatureValid(res)) {
				throw new MipsException(RespCode.INTERNAL_ERROR, "Invalid signature for response " + res.toJson()); 
			}
			return res;
		} 
		catch (Exception e) {
			throw new MipsException(RespCode.INTERNAL_ERROR, "nativeAPI call for echo (POST) operation failed: ", e); 
		}
	}
	
	@Override
	public CustRes customerInfo(String customerId) throws MipsException {
		try {
			CustReq req = new CustReq(merchantId, customerId);
			cryptoService.createSignature(req);
			Response response = nativeApiV16Resource.customerInfo(req.merchantId, req.customerId, req.dttm, URLEncoder.encode(req.signature, "UTF-8"));
			if (response == null || response.getStatus() != 200) {
				throw new MipsException(RespCode.INTERNAL_ERROR, "No response from nativeAPI for echo (POST) operation, http response " + (response != null ? response.getStatus() : "--")); 
			}
			CustRes res = response.readEntity(CustRes.class);
			if (!cryptoService.isSignatureValid(res)) {
				throw new MipsException(RespCode.INTERNAL_ERROR, "Invalid signature for response " + res.toJson()); 
			}
			return res;
		} 
		catch (Exception e) {
			throw new MipsException(RespCode.INTERNAL_ERROR, "nativeAPI call for customerInfo operation failed: ", e); 
		}
	}

	@Override
    public PayRes paymentOneclickInit(File recurrentFile) throws MipsException {
		try {
			Gson gson = new GsonBuilder().create();
			PayOneclickInitReq req = gson.fromJson(FileUtils.readFileToString(recurrentFile, "UTF-8"), PayOneclickInitReq.class);
			req.merchantId = merchantId;
			cryptoService.createSignature(req);
			Response response = nativeApiV16Resource.paymentOneclickInit(req);
			if (response == null || response.getStatus() != 200) {
				throw new MipsException(RespCode.INTERNAL_ERROR, "No response from nativeAPI for paymentOneclickInit operation, http response " + (response != null ? response.getStatus() : "--")); 
			}
			PayRes res = response.readEntity(PayRes.class);
			if (!cryptoService.isSignatureValid(res)) {
				throw new MipsException(RespCode.INTERNAL_ERROR, "Invalid signature for response " + res.toJson()); 
			}
			return res;
		} 
		catch (Exception e) {
			throw new MipsException(RespCode.INTERNAL_ERROR, "nativeAPI call for paymentOneclickInit operation failed: ", e); 
		}
    }

	@Override
	public PayRes paymentOneclickStart(String payId) throws MipsException {
		try {
			PayReq req = new PayReq(merchantId, payId);
			cryptoService.createSignature(req);
			Response response = nativeApiV16Resource.paymentOneclickStart(req);
			if (response == null || response.getStatus() != 200) {
				throw new MipsException(RespCode.INTERNAL_ERROR, "No response from nativeAPI for paymentOneclickStart operation, http response " + (response != null ? response.getStatus() : "--")); 
			}
			PayRes res = response.readEntity(PayRes.class);
			if (!cryptoService.isSignatureValid(res)) {
				throw new MipsException(RespCode.INTERNAL_ERROR, "Invalid signature for response " + res.toJson()); 
			}
			return res;
		} 
		catch (Exception e) {
			throw new MipsException(RespCode.INTERNAL_ERROR, "nativeAPI call for paymentOneclickStart operation failed: ", e); 
		}
	}
	
}
