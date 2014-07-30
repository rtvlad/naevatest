package tsol.mentes.twitter;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
 
public class ApiRestTweet {
  static String consumerKeyStr = "r3cKa2FLDnp5Vw6HhzswclpKF";
  static String consumerSecretStr = "rnvLn2eWjFO5rGbAopRieVHQtAiCdEvpy0LZ4RvigDEB95sEa2";
  static String accessTokenStr = "282224782-RfOHRNjOuF40w59KGRaDuukBUGfe4q06fSNc520u";
  static String accessTokenSecretStr = "F3UtiDuiGQc4xIiH0MK4I13LtoiIMexvbfQfYHvoIEnKU";
 
 
  public static void main(String[] args) throws Exception {
	  
	String tweet = "Desarrollo%20para%20la%20implementacion%20de%20twitter%20en%20sistema%20MenTeS";
	//guardamos en el objeto consumer las claves y tokens de acceso  
    OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(consumerKeyStr,
        consumerSecretStr);
    oAuthConsumer.setTokenWithSecret(accessTokenStr, accessTokenSecretStr);
 
    //generamos la llamada POST al REST de twitter que actualiza el estado
    HttpPost httpPost = new HttpPost(
        "https://api.twitter.com/1.1/statuses/update.json?status=" + tweet);
 
    oAuthConsumer.sign(httpPost);
 
    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponse httpResponse = httpClient.execute(httpPost);
 
    int statusCode = httpResponse.getStatusLine().getStatusCode();
    System.out.println(statusCode + ':'
        + httpResponse.getStatusLine().getReasonPhrase());
    System.out.println(IOUtils.toString(httpResponse.getEntity().getContent()));
 
  }
}