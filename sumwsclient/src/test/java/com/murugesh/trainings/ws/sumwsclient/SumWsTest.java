package com.murugesh.trainings.ws.sumwsclient;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.junit.Test;

import com.murugesh.trainings.ws.SumRequest;
import com.murugesh.trainings.ws.SumResponse;
import com.murugesh.trainings.ws.SumWSService;
import com.murugesh.trainings.ws.SumWs;

public class SumWsTest {

	@Test
	public void calculateSumShouldReturnValidResponse() {
		try {
			SumWSService service = new SumWSService((new URL("http://localhost:8080/sumws/services/sumService?wsdl")));
			SumWs port = service.getSumWsPort();

			Client client = ClientProxy.getClient(port);
			Endpoint endpoint = client.getEndpoint();

			HashMap<String, Object> outProps = new HashMap<String, Object>();
			outProps.put(WSHandlerConstants.ACTION, "UsernameToken Encrypt Signature Timestamp");
			outProps.put(WSHandlerConstants.USER, "sumuser");
			outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
			outProps.put(WSHandlerConstants.PW_CALLBACK_CLASS, PasswordCallbackHandler.class.getName());

			outProps.put(WSHandlerConstants.ENCRYPTION_USER, "myservicekey");
			outProps.put(WSHandlerConstants.ENC_PROP_FILE, "etc/clientKeystore.properties");
//			outProps.put(WSHandlerConstants.ENCRYPTION_PARTS, "{Element} {http://www.w3.org/2000/09/xmldsig#}Signature");

			outProps.put(WSHandlerConstants.SIGNATURE_USER, "myclientkey");
			outProps.put(WSHandlerConstants.SIG_PROP_FILE, "etc/clientKeystore.properties");
			
			outProps.put("timeToLive", "30");

			WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
			endpoint.getOutInterceptors().add(wssOut);

			HashMap<String, Object> inProps = new HashMap<String, Object>();
			inProps.put(WSHandlerConstants.ACTION, "Encrypt Signature Timestamp");
			inProps.put(WSHandlerConstants.DEC_PROP_FILE, "etc/clientKeystore.properties");
			inProps.put(WSHandlerConstants.PW_CALLBACK_CLASS, PasswordCallbackHandler.class.getName());
			inProps.put(WSHandlerConstants.SIG_PROP_FILE, "etc/clientKeystore.properties");

			WSS4JInInterceptor wssIn = new WSS4JInInterceptor(inProps);
			endpoint.getInInterceptors().add(wssIn);

			SumRequest request = new SumRequest();
			request.setNum1(10);
			request.setNum2(20);

			SumResponse response = port.calculateResponse(request);
			assertNotNull(response);
			assertEquals(30, response.getResult());

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
}
