package tsol.mentes.twitter;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
 
public class DirectMessagesTwitter {
  static String consumerKeyStr = "r3cKa2FLDnp5Vw6HhzswclpKF";
  static String consumerSecretStr = "rnvLn2eWjFO5rGbAopRieVHQtAiCdEvpy0LZ4RvigDEB95sEa2";
  static String accessTokenStr = "282224782-RfOHRNjOuF40w59KGRaDuukBUGfe4q06fSNc520u";
  static String accessTokenSecretStr = "F3UtiDuiGQc4xIiH0MK4I13LtoiIMexvbfQfYHvoIEnKU";
 
 
  public static void main(String[] args) throws Exception {
	  
	String tweet = "Te%20envio%20un%20mensaje%20directo%20con%20la%20oferta%20de%20CORTEFIEL%202X1%20en%20ropa%20de%20chica";
	String usuarioDestino = "naevatest";
	//guardamos en el objeto consumer las claves y tokens de acceso  
    OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(consumerKeyStr,
        consumerSecretStr);
    oAuthConsumer.setTokenWithSecret(accessTokenStr, accessTokenSecretStr);
 
    //generamos la llamada POST al REST de twitter que envia un mensaje directo a un usuario
    HttpPost httpPost = new HttpPost(
        "https://api.twitter.com/1.1/direct_messages/new.json?screen_name="+usuarioDestino+"&text="+tweet);
 
    oAuthConsumer.sign(httpPost);
 
    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponse httpResponse = httpClient.execute(httpPost);
 
    int statusCode = httpResponse.getStatusLine().getStatusCode();
    System.out.println(statusCode + ':'
        + httpResponse.getStatusLine().getReasonPhrase());
    System.out.println(IOUtils.toString(httpResponse.getEntity().getContent()));
 
  }
}