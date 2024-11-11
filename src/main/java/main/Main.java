/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main;

import bots.DiscordBot;
import java.sql.SQLException;
import java.util.List;
import javax.security.auth.login.LoginException;
import parser.CPMParser;

/**
 *
 * @author mixas
 */
public class Main {

    public static void main(String[] args) throws LoginException, InterruptedException, SQLException {
        DiscordBot dsBot = new DiscordBot();
        CPMParser parser = new CPMParser();
        
        String filePath40K = "pricesMap40k.dat";
        parser.loadPriceMapFromFile(filePath40K);
        List<String> last = parser.firstParse(dsBot);
          System.out.println("Закончило");
//      last = parser.firstParse(dsBot);

        int count = 0;
        while (true) {
            last = parser.loopParse(last);
            
            count++;
            Thread.sleep(300000);
            System.out.println(count);
            if (count == 3)
            {
                parser.savePriceMapToFile(filePath40K);
                count = 0;
            }
        }
    }
}
