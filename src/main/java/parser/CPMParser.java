/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package parser;

/**
 *
 * @author mixas
 */
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import bots.DiscordBot;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class CPMParser {

    public static Map<String, Integer> categoryMap = new HashMap<>();
    public static Map<String, Map<String, Integer>> pricesMap = new HashMap<>();
    public static List<String> newItems = new ArrayList<>();
    public static bots.DiscordBot dsBot;

    public static Map<String, Map<String, Integer>> getPricesMap() {
        return pricesMap;
    }

    public static List<String> firstParse(DiscordBot ds) throws SQLException {
        dsBot = ds;

        String url = "https://minifreemarket.com/catalog/games-with-miniatures/warhammer-40000?sorter=pub_date&fi_auction=0&fi_material[]=551&fi_model-status[]=553&fi_model-status[]=554&fi_model-status[]=555&fi_region_country=0&fi_tags=all&perpage=96";
        try {
            int count = 0;
            while (true) {

                Document doc = Jsoup.connect(url).get();

                if (doc.equals(null)) {
                    while (doc.equals(null)) {
                        doc = Jsoup.connect(url).get();
                    }
                }

                parse(doc);

                Element link = doc.select("div.pager a.next").first();
                System.out.println(count++);

                if (doc.select("div.pager a.next").isEmpty()) {
                    Elements items = doc.select("div.product");
                    List<String> itemsR = new ArrayList<>();
                    for (int i = 0; i < 5; i++) {
                        if (items.size() < i) {
                            itemsR.add("123");
                            continue;
                        }
                        if (items.get(i).select("div.product a.img").attr("href") == null) {
                            itemsR.add("123");
                        } else {
                            itemsR.add(items.get(i).select("div.product a.img").attr("href"));
                        }
                    }
                    items.clear();
                    return itemsR;
                }
                url = "https://minifreemarket.com/catalog/games-with-miniatures/warhammer-40000" + link.attr("href").replace("%5B0%5D", "[]").replace("%5B2%5D", "[]").replace("%5B1%5D", "[]").replace("%5B%5D", "[]");
                doc.empty();
            }

        } catch (IOException e) {
            System.out.println(url);
            e.printStackTrace();
        }
        return null;
    }

    public static void setPricesMap(Map<String, Map<String, Integer>> pricesMap) {
        CPMParser.pricesMap = pricesMap;
    }

    public static List<String> loopParse(List<String> lastItem) throws SQLException {
        String url = "https://minifreemarket.com/catalog/games-with-miniatures/warhammer-40000?sorter=pub_date-d&fi_auction=0&fi_material[]=551&fi_model-status[]=553&fi_model-status[]=554&fi_model-status[]=555&fi_region_country=0&fi_tags=all&perpage=96";
        try {

            while (true) {

                Document doc = Jsoup.connect(url).get();
                if (doc.equals(null)) {
                    while (doc.equals(null)) {
                        doc = Jsoup.connect(url).get();
                    }
                }

                List<String> item = parse(doc, lastItem);

                return item;

            }

        } catch (IOException e) {
            System.out.println(url);
            e.printStackTrace();
        }
        return null;
    }

    public static void parse(Document doc) throws SQLException {

        Elements items = doc.select("div.product");
        for (Element item : items) {
            String category = item.select("div.product div.info a.cat_title").text();

            String name = clearname(item.select("div.product div.info a.product_title").text());
            String temp = item.select("div.product div.price").first().ownText().trim();

            int price = Integer.parseInt(removeChar(temp));

            if (!pricesMap.containsKey(category)) {
                pricesMap.put(category, new HashMap<>());
            }

            if (pricesMap.get(category).containsKey(name)) {                
                pricesMap.get(category).put(name, (price + pricesMap.get(category).get(name)) / 2);
            } else {
                pricesMap.get(category).put(name, price);
            }
        }

    }

    public static List<String> parse(Document doc, List<String> last) throws SQLException {

        Elements items = doc.select("div.product");

        for (Element item : items) {

            if (last.contains(item.select("div.product a.img").attr("href"))) {
                break;
            }

            String category = item.select("div.product div.info a.cat_title").text();

            String name = clearname(item.select("div.product div.info a.product_title").text());
            String temp = item.select("div.product div.price").first().ownText().trim();

            int price = Integer.parseInt(removeChar(temp));

            if (!pricesMap.containsKey(category)) {
                pricesMap.put(category, new HashMap<>());
            }

            if (pricesMap.get(category).containsKey(name)) {
                double averagePrice = pricesMap.get(category).get(name);
                double priceDifference = averagePrice - price;
                double percentageDifference = (priceDifference / averagePrice) * 100;

                if (averagePrice * 0.5 > price) {
                    dsBot.message(category, "\nОчень низкая цена на товар: " + name
                            + "\nЦена: " + price
                            + "\nСредняя цена: " + averagePrice
                            + "\nНиже средней цены на: " + priceDifference
                            + "\nПроцент ниже средней цены: " + String.format("%.2f", percentageDifference) + "%"
                            + "\nСсылка: " + "https://minifreemarket.com" + item.select("div.product a.img").attr("href"), 1);
                } else if (averagePrice * 0.75 > price) {
                    dsBot.message(category, "\nНизкая цена на товар: " + name
                            + "\nЦена: " + price
                            + "\nСредняя цена: " + averagePrice
                            + "\nНиже средней цены на: " + priceDifference
                            + "\nПроцент ниже средней цены: " + String.format("%.2f", percentageDifference) + "%"
                            + "\nСсылка: " + "https://minifreemarket.com" + item.select("div.product a.img").attr("href"), 1);
                }
                pricesMap.get(category).put(name, (price + pricesMap.get(category).get(name)) / 2);
            } else {
                dsBot.message(category, "\nНовый товар: " + name
                        + "\nЦена: " + price
                        + "\nСсылка: " + "https://minifreemarket.com" + item.select("div.product a.img").attr("href"), 2);
                pricesMap.get(category).put(name, price);
            }
        }
        List<String> itemsR = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            itemsR.add(items.get(i).select("div.product a.img").attr("href"));
        }
        items.clear();
        return itemsR;

    }

    public static String removeChar(String str) {
        String trimmedStr = str.replaceAll(" ", "");
        return trimmedStr.replace("e", "");
    }

    public static String clearname(String name) {
        String result = name.trim();
        result = result.replaceAll("\\+", "");
        result = result.replaceAll("\\(", "");
        result = result.replaceAll("\\)", "");
        return result.trim();
    }


    public static void savePriceMapToFile(String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(pricesMap);
            System.out.println("Файл средних цен сохраннен.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ошибка при сохранении файла.");
        }
    }

    @SuppressWarnings("unchecked")
    public static void loadPriceMapFromFile(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            pricesMap = (Map<String, Map<String, Integer>>) ois.readObject();
            System.out.println("Файл средних цен загружен.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Ошибка загрузки файла средних цен.");
            pricesMap = new HashMap<>();
        }
    }
}
