package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class App {

    public static void main(String[] args) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonTickets = mapper.readTree(new File("tickets.json")).get("tickets");

            List<Ticket> tickets = extractTickets(jsonTickets);

            for (String carrier : getCarriers(tickets)) {
                int minFlightTime = calculateMinFlightTime(tickets, "Владивосток", "Тель-Авив", carrier);
                System.out.println("Минимальное время для перевозчика " + carrier + ": " + minFlightTime + " минут");
            }

            double avgPrice = calculateAveragePrice(tickets, "Владивосток", "Тель-Авив");
            double medianPrice = calculateMedianPrice(tickets, "Владивосток", "Тель-Авив");

            System.out.println(
                    "Разность между средней ценой и медианой (Владивосток - Тель-Авив): " +
                            Math.abs(avgPrice - medianPrice)
            );

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private static List<Ticket> extractTickets(JsonNode jsonTickets) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy HH:mm");
        List<Ticket> tickets = new ArrayList<>();

        for (JsonNode jsonTicket : jsonTickets) {
            String originName = jsonTicket.get("origin_name").asText();
            String destinationName = jsonTicket.get("destination_name").asText();
            String carrier = jsonTicket.get("carrier").asText();
            int stops = jsonTicket.get("stops").asInt();
            int price = jsonTicket.get("price").asInt();

            Ticket ticket = new Ticket(
                    originName, destinationName, carrier, stops, price,
                    sdf.parse(
                            jsonTicket.get("departure_date").asText() + " "
                                    + jsonTicket.get("departure_time").asText()
                    ),
                    sdf.parse(
                            jsonTicket.get("arrival_date").asText() + " "
                                    + jsonTicket.get("arrival_time").asText()
                    )
            );
            System.out.println("ticket.getArrivalTime() = " + ticket.getArrivalTime());
            tickets.add(ticket);
        }
        return tickets;
    }

    private static List<String> getCarriers(List<Ticket> tickets) {
        List<String> carriers = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (!carriers.contains(ticket.getCarrier())) {
                carriers.add(ticket.getCarrier());
            }
        }
        return carriers;
    }

    private static int calculateMinFlightTime(List<Ticket> tickets, String origin, String destination, String carrier) {
        int minFlightTime = Integer.MAX_VALUE;
        for (Ticket ticket : tickets) {
            if (ticket.getOriginName().equals(origin) && ticket.getDestinationName().equals(destination)
                    && ticket.getCarrier().equals(carrier)) {
                int flightTime = (int) (
                        (ticket.getArrivalTime().getTime() - ticket.getDepartureTime().getTime()) / (60 * 1000)
                );
                if (flightTime < minFlightTime) {
                    minFlightTime = flightTime;
                }
            }
        }
        return minFlightTime;
    }

    private static double calculateAveragePrice(List<Ticket> tickets, String origin, String destination) {
        int totalPrice = 0;
        int count = 0;
        for (Ticket ticket : tickets) {
            if (ticket.getOriginName().equals(origin) && ticket.getDestinationName().equals(destination)) {
                totalPrice += ticket.getPrice();
                count++;
            }
        }
        return count == 0 ? 0 : (double) totalPrice / count;
    }

    private static double calculateMedianPrice(List<Ticket> tickets, String origin, String destination) {
        List<Integer> prices = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket.getOriginName().equals(origin) && ticket.getDestinationName().equals(destination)) {
                prices.add(ticket.getPrice());
            }
        }

        Collections.sort(prices);

        int middle = prices.size() / 2;
        if (prices.size() % 2 == 1) {
            return prices.get(middle);
        } else {
            return (double) (prices.get(middle - 1) + prices.get(middle)) / 2;
        }
    }

}

class Ticket {
    private final String originName;
    private final String destinationName;
    private final String carrier;
    private final int stops;
    private final int price;
    private final Date departureTime;
    private final Date arrivalTime;

    public Ticket(
            String originName, String destinationName, String carrier,
            int stops, int price, Date departureTime, Date arrivalTime
    ) {
        this.originName = originName;
        this.destinationName = destinationName;
        this.carrier = carrier;
        this.stops = stops;
        this.price = price;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    public String getOriginName() {
        return originName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public String getCarrier() {
        return carrier;
    }

    public int getStops() {
        return stops;
    }

    public int getPrice() {
        return price;
    }

    public Date getDepartureTime() {
        return departureTime;
    }

    public Date getArrivalTime() {
        return arrivalTime;
    }
}
