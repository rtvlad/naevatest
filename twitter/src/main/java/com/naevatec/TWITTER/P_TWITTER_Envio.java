package com.naevatec.TWITTER;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.naevatec.APIENVIO.Mensaje;
import com.naevatec.APIENVIO.Sending_Protocol;
import com.naevatec.ControlCaudal.ControlCaudal;
import com.naevatec.commons.MTSConsts;
import com.tsol.mentes.channel.persistence.MtsCanales;

public class P_TWITTER_Envio extends Thread implements Sending_Protocol {
	
	public static final String CLASE = "com.naevatec.APIENVIO.twitter";

	private int idCanal = 1;
	private String servidor = null;
	private String usuario = null;
	private String remitente = null;
	private String clave = null;
	private String URLRecepcion = null;
	private int MaxThreadsEnvio = 0;

	private Log logger = LogFactory.getLog(P_TWITTER_Envio.class.getName());

	public static boolean escaparCaracteres = false;

	private ExecutorService execLocal = null;
	private ControlCaudal Caudalimetro = null;

	private AtomicInteger activeSendings = new AtomicInteger();

	public P_TWITTER_Envio() {
		
	}
	
	@Override
	public List<Mensaje> envia(List<Mensaje> msgList) throws Exception {
		try {
			logger.debug("Canal: " + idCanal
					+ " ::Increment active sendings. Actives:: "
					+ activeSendings.incrementAndGet());

			this.logger.info("TWITTER_IS::Envia(msgList)_" + this.idCanal
					+ ":: Num_mensajes [" + msgList.size() + "]::Inicio");
			
			CountDownLatch countDown = new CountDownLatch(msgList.size());

			for (int i = 0; i < msgList.size(); i++) {
				Mensaje mensaje = msgList.get(i);

				if (mensaje.getNumDestinatarios() <= 0) {
					this.logger
							.warn("TWITTER_IS::Envia(msgList)_"
									+ this.idCanal
									+ "::No hay ningun destinatario para este mensaje. idEnvQueue["
									+ mensaje.getIdEnvQueue() + "]");
					countDown.countDown();
					continue;
				}
				
				mensaje.setTexto(codificarMensaje(mensaje.getTexto()));;
				execLocal.execute(new SubmitReqTwitter(i, mensaje, this.logger, countDown, this.Caudalimetro));

			}
	
			logger.debug("Canal:: " + idCanal
					+ " -Check countdown::Waiting some action executing:: "
					+ countDown.getCount() + " actions.");
			if (!countDown
					.await(MTSConsts.SENDING_AWAIT_TIME, TimeUnit.SECONDS))
				logger.warn("Canal:: " + idCanal
						+ " -There are some action executing:: "
						+ countDown.getCount() + " actions.");
			else
				logger.debug("Canal:: " + idCanal
						+ " -No more actions executing.");
			logger.debug("Canal:: " + idCanal
					+ " -Check countdown::Waiting finalized.");

			this.logger.info("MIB_IS::Envia(msgList)_" + this.idCanal
					+ "::::Final--------------------------------");
		} finally {
			logger.debug("Canal: " + idCanal
					+ " ::Decrement active sendings. Actives:: "
					+ activeSendings.decrementAndGet());
		}
		return msgList;
	}
	
	public String codificarMensaje(String twt){
		String tweet;
		tweet = twt.replaceAll(" ", "%20");	
		return tweet;
	}
	
	@Override
	public void setIdCanal(int idCanal) {
		this.idCanal = idCanal;
	}

	@Override
	public void setUsuario(String usuario) {
		this.usuario = usuario;

	}

	@Override
	public void setRemitente(String remitente) {
		this.remitente = remitente;
	}

	@Override
	public void setClave(String clave) {
		this.clave = clave;

	}

	@Override
	public void setURLRecepcion(String URLRecepcion) {
		this.URLRecepcion = URLRecepcion;
	}

	@Override
	public void setMaxThreadsEnvio(int MaxThreadsEnvio) {
		this.MaxThreadsEnvio = MaxThreadsEnvio;
		this.execLocal = Executors.newFixedThreadPool(this.MaxThreadsEnvio);
	}

	@Override
	public int getIdCanal() {
		return this.idCanal;
	}

	@Override
	public String getServidor() {
		return this.servidor;
	}

	@Override
	public String getUsuario() {
		return this.usuario;
	}

	@Override
	public String getRemitente() {
		return this.remitente;
	}

	@Override
	public String getClave() {
		return this.clave;
	}

	@Override
	public String getURLRecepcion() {
		return this.URLRecepcion;
	}

	@Override
	public int getMaxThreadsEnvio() {
		return this.MaxThreadsEnvio;
	}

	@Override
	public ControlCaudal getCaudalimetro() {
		return this.Caudalimetro;
	}

	@Override
	public void initCaudalimetro(MtsCanales canal) {
		this.Caudalimetro = new ControlCaudal(canal.getId(), canal.getCaudal(),
				this.logger);
	}

	@Override
	public void finalizeSending() {
		logger.info("Finalizing P_MIB_IS_Envio:: Canal " + idCanal);

		logger.info("Canal " + idCanal
				+ " Waiting for finishing all active sendings. There are "
				+ activeSendings.get() + " active sendings.");

		while (activeSendings.get() > 0) {
			try {
				logger.info("Canal " + idCanal
						+ " Checking active sendings. Sleep "
						+ MTSConsts.ACTIVE_SENDING_CHECK_TIME + " milis from "
						+ Thread.currentThread().getName());
				Thread.sleep(MTSConsts.ACTIVE_SENDING_CHECK_TIME);
			} catch (InterruptedException e) {
				logger.warn("Exception ocurred.", e);
			}
		}

		logger.info("Canal " + idCanal + " There are no active sendings.");

		if (execLocal != null && !execLocal.isShutdown()) {
			execLocal.shutdown();
			try {
				if (!execLocal.awaitTermination(
						MTSConsts.EXECUTOR_SHUTDOWN_AWAIT_TIME,
						TimeUnit.SECONDS))
					logger.warn("Canal:: " + idCanal
							+ " -Could not shutdown the executor.");

			} catch (InterruptedException e) {
				logger.warn("Canal:: " + idCanal
						+ "Error ocurred while shutdown the executor.");
			}
			execLocal = null;
		}
		logger.info("Finalized P_MIB_IS_Envio:: Canal " + idCanal);
	}

	@Override
	public void setServidor(String servidor) {
		// TODO Auto-generated method stub
		
	}
	
}