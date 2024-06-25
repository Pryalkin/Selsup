package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApi {

    private static final String BASE_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private static final Gson GSON = new GsonBuilder().create();
    private final Semaphore semaphore;
    private final Instant lastRequestTime;
    private final Duration timeUnit;
    private final int requestLimit;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        if (requestLimit <= 0) {
            throw new IllegalArgumentException("Request limit must be positive.");
        }
        this.timeUnit = Duration.of(requestLimit, timeUnit.toChronoUnit());
        this.requestLimit = requestLimit;
        this.semaphore = new Semaphore(requestLimit);
        this.lastRequestTime = Instant.now();
    }

    public void createDocument(Document document, String signature) throws InterruptedException, IOException {
        try {
            semaphore.acquire();
            if (Instant.now().isBefore(lastRequestTime.plus(timeUnit))) {
                semaphore.release();
                Thread.sleep(timeUnit.toMillis() - Duration.between(lastRequestTime, Instant.now()).toMillis());
                semaphore.acquire();
            }
            sendRequest(document, signature);
            lastRequestTime.plus(timeUnit);
            semaphore.release();
        } catch (IOException e) {
            semaphore.release();
            throw e;
        }
    }

    private void sendRequest(Document document, String signature) throws IOException {
        RequestBody requestBody = RequestBody.create(
                GSON.toJson(document),
                MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(requestBody)
                .addHeader("Authorization", signature)
                .build();
        try (Response response = new OkHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("Message: " + response.message());
            }
            //
        }
    }

    public static void main (String[]args) throws IOException, InterruptedException {
        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 10);
        Description description = new Description("string");
        Product product = new Product("string", "2020-01-23",
                "string","string","string",
                "2020-01-23", "string", "string", "string");
        Product[] products = new Product[10];
        products[0] = product;
        Document document = new Document(description, "string", "string",
                "string", "string", "string",
                "2020-01-23", "string", products,
                "2020-01-23", "string");
        crptApi.createDocument(document, "test");
    }
}


    class Document {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type = "LP_INTRODUCE_GOODS";
        private boolean importRequest = true;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private String production_date;
        private String production_type;
        private Product[] products;
        private String reg_date;
        private String reg_number;

        public Document(Description description, String doc_id, String doc_status,
                        String owner_inn, String participant_inn, String producer_inn,
                        String production_date, String production_type,
                        Product[] products, String reg_date, String reg_number) {
            this.description = description;
            this.doc_id = doc_id;
            this.doc_status = doc_status;
            this.owner_inn = owner_inn;
            this.participant_inn = participant_inn;
            this.producer_inn = producer_inn;
            this.production_date = production_date;
            this.production_type = production_type;
            this.products = products;
            this.reg_date = reg_date;
            this.reg_number = reg_number;
        }
    }

    class Description{
        private String participantInn;

        public Description(String participantInn){
            this.participantInn = participantInn;
        }
    }

    class Product {
        private String certificate_document;
        private String certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private String production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;

        public Product(String certificate_document, String certificate_document_date, String certificate_document_number, String owner_inn, String producer_inn, String production_date, String tnved_code, String uit_code, String uitu_code) {
            this.certificate_document = certificate_document;
            this.certificate_document_date = certificate_document_date;
            this.certificate_document_number = certificate_document_number;
            this.owner_inn = owner_inn;
            this.producer_inn = producer_inn;
            this.production_date = production_date;
            this.tnved_code = tnved_code;
            this.uit_code = uit_code;
            this.uitu_code = uitu_code;
        }
    }




