package com.bitstrat.stripe;

import java.nio.file.Paths;

import static spark.Spark.post;
import static spark.Spark.port;
import static spark.Spark.staticFiles;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

public class Server {

  public static void main(String[] args) {
    port(4242);

    // This is your test secret API key.
    Stripe.apiKey = "REMOVED";

    staticFiles.externalLocation(
        Paths.get("public").toAbsolutePath().toString());

    post("/create-checkout-session", (request, response) -> {
        String YOUR_DOMAIN = "http://localhost:4242";
        SessionCreateParams params =
          SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(YOUR_DOMAIN + "/success.html")
            .setAutomaticTax(
              SessionCreateParams.AutomaticTax.builder()
                .setEnabled(true)
                .build())
            .addLineItem(
              SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                // Provide the exact Price ID (for example, price_1234) of the product you want to sell
                .setPrice("price_1Se6ao01oMr05NTU2wczaoZp")
                .build())
            .build();
      Session session = Session.create(params);

      response.redirect(session.getUrl(), 303);
      return "";
    });
  }
}
