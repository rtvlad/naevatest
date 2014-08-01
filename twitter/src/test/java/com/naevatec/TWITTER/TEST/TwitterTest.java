package com.naevatec.TWITTER.TEST;




import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.naevatec.APIENVIO.Mensaje;
import com.naevatec.APIENVIO.Sending_Protocol;
import com.naevatec.APIENVIO.APIMIB_IS.test.Test;
import com.naevatec.TWITTER.P_TWITTER_Envio;
import com.tsol.mentes.channel.persistence.MtsCanales;

public class TwitterTest extends Thread{
	
	private static final Log logger = LogFactory.getLog(Test.class);
	public static final String CLASE = "com.naevatec.TWITTER.P_TWITTER_Envio";
	
	public static void main(String[] args) throws Exception{
		
		int MaxThreadsEnvio = 5;
		int caudal = 2000;
		int id = 1;
		
		Sending_Protocol protocol = cargaClase(CLASE);
		if (protocol != null) {
			
			protocol.setIdCanal(id);
			protocol.setMaxThreadsEnvio(MaxThreadsEnvio);

			MtsCanales canal = new MtsCanales();
			canal.setId(id);
			canal.setCaudal(caudal);
			protocol.initCaudalimetro(canal);
		} else {
			logger.error("cargaProtocolo(): Protocol with channel ID " + id
					+ " could not be loaded");
			throw new Exception("cargaProtocolo(): Protocol with channel ID "
					+ id + " could not be loaded");

		}
		Mensaje mensaje1 = new Mensaje();
		Mensaje mensaje2 = new Mensaje();
		
		mensaje1.setDestinatario("naevatest");
		mensaje1.setTexto("envio java 1");
		
		mensaje2.setDestinatario("naevatest");
		mensaje2.setTexto("envio java 2");
		
		List<Mensaje> msgList = new ArrayList<Mensaje>();
		msgList.add(mensaje1);
		msgList.add(mensaje2);
		
		try {
			msgList = protocol.envia(msgList);

			logger.debug("-------------RESULTADO DEL ENVIO----------INI");
			for (int i = 0; i < msgList.size(); i++) {
				logger.debug("Mensaje Nº:: " + (i + 1));
				logger.debug("Destinatario(s) ["
						+ msgList.get(i).getNumDestinatarios() + "]:: "
						+ msgList.get(i).getDestinatario().toString());
				logger.debug("Estado:: " + msgList.get(i).getEstado());
				logger.debug("Id_Tms:: " + msgList.get(i).getid_tms());
				logger.debug("Tiempo:: " + msgList.get(i).getTiempo());
			}
			logger.debug("-------------RESULTADO DEL ENVIO----------FIN");
		} catch (Exception e1) {
			logger.warn("main(): " + e1.toString());
		}
	}
	
	private static Sending_Protocol cargaClase(String protocolo) {
		Sending_Protocol protoc = null;
		try {
			Class clase = Class.forName(protocolo);
			protoc = (Sending_Protocol) clase.newInstance();
		} catch (ClassNotFoundException e) {
			logger.error("Clase no encontrada " + protocolo, e);
		} catch (InstantiationException ex) {
			logger.error("Transformacion no realizada", ex);
		} catch (IllegalAccessException exc) {
			logger.error("Acceso ilegal", exc);
		}

		return protoc;

	}

}