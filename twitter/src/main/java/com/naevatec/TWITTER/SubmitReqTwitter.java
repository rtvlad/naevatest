package com.naevatec.TWITTER;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.commons.logging.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import com.naevatec.APIENVIO.Mensaje;
import com.naevatec.ControlCaudal.ControlCaudal;
import com.naevatec.commons.MTSConsts;

import es.telefonica.mi.interfazsimplificado.schemas.SubmitRes;

public class SubmitReqTwitter extends Thread {

	public static final String BINARY_SMS_CHARSET = "UTF-16";
	public static final String MIB_IS_VERSION = "1.0";
	public static final String content_type = "text/html;charset=ISO-8859-1";
	public static final String charset = "ISO-8859-1";

	private int numProc = -1;
	private Mensaje mensaje = null;
	private Log logger = null;
	private CountDownLatch countDown = null;
	private ControlCaudal caudalimetro = null;
	
	private String consumerKeyStr = "r3cKa2FLDnp5Vw6HhzswclpKF";
	private String consumerSecretStr = "rnvLn2eWjFO5rGbAopRieVHQtAiCdEvpy0LZ4RvigDEB95sEa2";
	private String accessTokenStr = "282224782-RfOHRNjOuF40w59KGRaDuukBUGfe4q06fSNc520u";
	private String accessTokenSecretStr = "F3UtiDuiGQc4xIiH0MK4I13LtoiIMexvbfQfYHvoIEnKU";

	public SubmitReqTwitter(int numProc, Mensaje mensaje, Log logger, CountDownLatch countDown, ControlCaudal caudalimetro) {
		this.numProc = numProc;
		this.logger = logger;
		this.mensaje = mensaje;
		this.countDown = countDown;
		this.caudalimetro = caudalimetro;
	}

	public void run() {
		try {		
			JSONObject resultadoEnvio = new JSONObject();
			String messageId = "";
			String statusCode = "";

			try {
				logger.info("Enviando el índice " + numProc
						+ " de la lista de mensajes");

				resultadoEnvio = envia(mensaje);
				
				if (resultadoEnvio != null) {
					messageId = resultadoEnvio.getString("id_str");
					statusCode = "1000";
				} else {
					logger.error("Error al enviar mensaje");
					statusCode = "3000";
					messageId = "0";
				}

			} catch (Exception eEnviando) {
				logger.error("El destinatario del mensaje es incorrecto porque no existe o no es follower de la aplicacion");
				statusCode = "999";
				messageId = "0";
			}
			
			String state = "";
			if (statusCode.equals("1000")) {// Envio Correcto
				state = MTSConsts.SENDING_STATE_ESTADO_ENVIO_CORRECTO;
			}else{
				state = Mensaje.ESTADO_ERROR_INDEFINIDO + statusCode;
			}
			mensaje.setEstado(state);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			String tiempo = sdf.format(new Date());
			if (messageId != null && !messageId.equals(""))
				mensaje.setid_tms(messageId);
			else {
				mensaje.setid_tms("0");
				logger.error("SubmitReqConcurrente(): messageId = " + messageId);
			}

			mensaje.setIdentificador(mensaje.getIdEnvQueue());
			mensaje.setTiempo(tiempo);

			logger.debug("Mensaje Parseado: ID_Envio["
					+ mensaje.getIdentificador() + "] Tiempo["
					+ mensaje.getTiempo() + "] Confirmacion["
					+ mensaje.getid_tms() + "]");

		} finally {
			countDown.countDown();
		}
	}

	private JSONObject envia(Mensaje mensaje) throws Exception {
		try {
			logger.debug("Enviando....");
			caudalimetro.entro();
			InputStream is = null;
			String result = "";
			long time_ini = System.currentTimeMillis();

			//guardamos en el objeto consumer las claves y tokens de acceso  
		    OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(consumerKeyStr,
		        consumerSecretStr);
		    oAuthConsumer.setTokenWithSecret(accessTokenStr, accessTokenSecretStr);
		 
		    //generamos la llamada POST al REST de twitter que envia un mensaje directo a un usuario
		    HttpPost httpPost = new HttpPost(
		        "https://api.twitter.com/1.1/direct_messages/new.json?screen_name="+mensaje.getDestinatario()+"&text="+mensaje.getTexto());
		 
		    oAuthConsumer.sign(httpPost);
		 
		    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		    HttpResponse httpResponse = httpClient.execute(httpPost);
		    System.out.println("El httpResponse: " + httpResponse.toString());
		    
		    HttpEntity entity = httpResponse.getEntity();
		    is = entity.getContent();
		    try{
		         BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
		         StringBuilder sb = new StringBuilder();
		         String line = null;
		         while ((line = reader.readLine()) != null) {
		             sb.append(line + "\n");
		         }
		         is.close();
		         result=sb.toString();
		     } catch(Exception e){}
		    
		    
		    JSONObject json = new JSONObject();
		    try{
		         json = new JSONObject(result);
	      	         		         
		     }catch(JSONException e){
		    	 e.getMessage();
		     }
   
			long time_fin = System.currentTimeMillis();
			logger.debug("Mensaje Enviado en " + (time_fin - time_ini) + " ms.");
			return json;
		} catch (RemoteException e) {
			logger.warn("enviaTexto(): Error sending MD: "
					+ mensaje.getIdEnvQueue() + " Exception = " + e);
			logger.debug(
					"enviaTexto(): Error sending MD: "
							+ mensaje.getIdEnvQueue(), e);
			return null;
		}
	}
}
