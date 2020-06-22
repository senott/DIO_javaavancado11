package br.com.nomadweb.main;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main {

    static ExecutorService executor = Executors.newFixedThreadPool(6, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            System.out.println("Nova thread criada :: " + ( thread.isDaemon() ? "daemon" : "") + " ThreadGroup :: " + thread.getThreadGroup());
            return thread;
        }
    });

    public static void main(String[] args) throws Exception {
        //connectAndPrintURL();
        //connectHttpClient();
        connectHttpClient2();
    }

    private static void connectHttpClient() throws Exception {

        System.out.println("Executando exemplo HTTP/1.1");

        try {
            HttpClient httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .proxy(ProxySelector.getDefault())
                    .build();

            long start = System.currentTimeMillis();

            HttpRequest mainRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://http2.akamai.com/demo/h2_demo_frame.html"))
                    .build();

            HttpResponse<String> response = httpClient.send(mainRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status code :: " + response.statusCode());
            System.out.println("Headers :: " + response.headers());
            System.out.println(response.body());

            List<Future<?>> future = new ArrayList<>();

            String responseBody = response.body();

            responseBody.lines().filter(line -> line.trim().startsWith("<img height"))
                    .map(line -> line.substring(line.indexOf("src='") + 5, line.indexOf("'/>")))
                    .forEach(image -> {
                        Future<?> imgFuture = executor.submit(() -> {
                            HttpRequest imgRequest = HttpRequest.newBuilder()
                                    .uri(URI.create("https://http2.akamai.com" + image))
                                    .build();

                            try {
                                HttpResponse<String> imgResponse = httpClient.send(imgRequest, HttpResponse.BodyHandlers.ofString());
                                System.out.println("Imagem carregada :: " + image + ", status code: " + imgResponse.statusCode());
                            } catch (InterruptedException | IOException e) {

                                e.printStackTrace();

                            }
                        });
                        future.add(imgFuture);
                        System.out.println("Submetido futuro para a imagem ::" + image);
                    });

            future.forEach( f -> {
                try {
                    f.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });

            long end = System.currentTimeMillis();

            System.out.println("Tempo execução: " + (end - start));

        } finally {

            executor.shutdown();

        }
    }

    private static void connectHttpClient2() throws Exception {

        System.out.println("Executando exemplo HTTP/2");

        try {
            HttpClient httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .proxy(ProxySelector.getDefault())
                    .build();

            long start = System.currentTimeMillis();

            HttpRequest mainRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://http2.akamai.com/demo/h2_demo_frame.html"))
                    .build();

            HttpResponse<String> response = httpClient.send(mainRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status code :: " + response.statusCode());
            System.out.println("Headers :: " + response.headers());
            System.out.println(response.body());

            List<Future<?>> future = new ArrayList<>();

            String responseBody = response.body();

            responseBody.lines().filter(line -> line.trim().startsWith("<img height"))
                    .map(line -> line.substring(line.indexOf("src='") + 5, line.indexOf("'/>")))
                    .forEach(image -> {
                        Future<?> imgFuture = executor.submit(() -> {
                            HttpRequest imgRequest = HttpRequest.newBuilder()
                                    .uri(URI.create("https://http2.akamai.com" + image))
                                    .build();

                            try {
                                HttpResponse<String> imgResponse = httpClient.send(imgRequest, HttpResponse.BodyHandlers.ofString());
                                System.out.println("Imagem carregada :: " + image + ", status code: " + imgResponse.statusCode());
                            } catch (InterruptedException | IOException e) {

                                e.printStackTrace();

                            }
                        });
                        future.add(imgFuture);
                        System.out.println("Submetido futuro para a imagem ::" + image);
                    });

            future.forEach( f -> {
                try {
                    f.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });

            long end = System.currentTimeMillis();

            System.out.println("Tempo execução: " + (end - start));

        } finally {

            executor.shutdown();

        }
    }

    private static void connectAndPrintURL() throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(URI.create("https://docs.oracle.com/javase/10/language"))
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status code :: " + response.statusCode());
        System.out.println("Headers :: " + response.headers());
        System.out.println(response.body());
    }
}
