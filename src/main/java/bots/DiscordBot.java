package bots;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author yamis
 */

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import javax.security.auth.login.LoginException;
import database.DatabaseConnect;

public class DiscordBot extends ListenerAdapter {

    public static Map<String, ArrayList<User>> userCategory = new HashMap<>();
    public Map<String, String> guildsChannels = new HashMap<>();
    public List<User> users = new ArrayList<>();
    public static JDA jda;

    private final DatabaseConnect db = new DatabaseConnect();

    public DiscordBot() throws LoginException {
        try {
            db.dbConnection();
        } catch (SQLException ex) {
            Logger.getLogger(DiscordBot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DiscordBot.class.getName()).log(Level.SEVERE, null, ex);
        }
        jda = JDABuilder.createDefault("")
                .addEventListeners(this)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();
    }

    public static void main(String[] args) throws LoginException {

        DiscordBot ds = new DiscordBot();
    }

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("Бот подключен к Discord!");

        try {
            for (String s : db.getAllUsers()) {
                users.add(jda.retrieveUserById(s).complete());
            }
        } catch (SQLException ex) {
            Logger.getLogger(DiscordBot.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            for (String s : db.getAllCategory()) {
                add(s);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DiscordBot.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            guildsChannels = db.getGuildsChannels();
        } catch (SQLException ex) {
            Logger.getLogger(DiscordBot.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (User use : users) {
            List<String> usercategory;
            try {
                usercategory = db.getUserCategory(use.getId());
                for (String c : usercategory) {
                    add(use, c);
                }
            } catch (SQLException ex) {
                Logger.getLogger(DiscordBot.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        if (event.getMessage().getContentRaw().contains(jda.getSelfUser().getAsMention())
                && (event.getMessage().getContentRaw().contains("Этот чат для парсера с СРМ"))) {
            if (guildsChannels.containsKey(event.getGuild().getId())) {
                return;
            }
            MessageChannelUnion channel = event.getChannel();
            User user = event.getAuthor();
            try {
                db.addGuildChannel(event.getGuild().getId(), channel.getId());
            } catch (SQLException ex) {
                Logger.getLogger(DiscordBot.class.getName()).log(Level.SEVERE, null, ex);
            }
            channel.sendMessage(user.getAsMention() + " ID чата записан").queue();
            guildsChannels.put(event.getGuild().getId(), channel.getId());
        }
        if (!guildsChannels.get(event.getGuild().getId()).equals(event.getChannel().getId())) {
            return;
        }
        if (event.getMessage().getContentRaw().contains(jda.getSelfUser().getAsMention())
                && (event.getMessage().getContentRaw().contains("Какие есть категории?") || event.getMessage().getContentRaw().contains("какие есть категории?"))) {

            User user = event.getAuthor();
            MessageChannelUnion channel = event.getChannel();
            channel.sendMessage(user.getAsMention() + "Все категории\n" + userCategory.keySet()).queue();
        }

        if (event.getMessage().getContentRaw().contains(jda.getSelfUser().getAsMention())
                && event.getMessage().getContentRaw().contains("Добавь мне категорию")) {
            User user = event.getAuthor();
            String category = event.getMessage().getContentRaw().toString();
            category = category.replaceAll(jda.getSelfUser().getAsMention(), "").trim();
            category = category.replaceAll("Добавь мне категорию", "").trim();
            MessageChannelUnion channel = event.getChannel();
            
            if (userCategory.containsKey(category)) {

                if (!userCategory.get(category).contains(user)) {

                    if (users.contains(user)) {
                        try {
                            if (db.addUserCatgeroy(user.getId(), category)) {
                                add(user, category);
                                event.getChannel().sendMessage(user.getAsMention() + " вам добавленна данная категория").queue();
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(DiscordBot.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else 
                        try {
                            db.addUser(user.getId(), user.getName());
                    } catch (SQLException ex) {
                        Logger.getLogger(DiscordBot.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        if (db.addUserCatgeroy(user.getId(), category)) {
                            add(user, category);
                            users.add(user);
                            event.getChannel().sendMessage(user.getAsMention() + " вам добавленна данная категория").queue();
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(DiscordBot.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    channel.sendMessage(user.getAsMention() + " данная категория уже вам добавлена").queue();
                }
            } else {
                channel.sendMessage(user.getAsMention() + " такой категории не существует").queue();
            }
        }

        if (event.getMessage().getContentRaw().contains(jda.getSelfUser().getAsMention())
                && event.getMessage().getContentRaw().contains("я хочу видеть")) {
            User user = event.getAuthor();
            String vising = event.getMessage().getContentRaw().toString();
            vising = vising.replaceAll("я хочу видеть", "").trim();
            vising = vising.replaceAll(jda.getSelfUser().getAsMention(), "").trim();
            MessageChannelUnion channel = event.getChannel();
            switch (vising) {
                case "только скидки": {
                    try {
                        db.updateVision(user.getId(), 1);
                        channel.sendMessage(user.getAsMention() + " Теперь вы видите только скидки").queue();
                    } catch (SQLException ex) {
                        Logger.getLogger(DiscordBot.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;

                case "только новое": {
                    try {
                        db.updateVision(user.getId(), 2);
                        channel.sendMessage(user.getAsMention() + " Теперь вы видите только новые товары").queue();
                    } catch (SQLException ex) {
                        Logger.getLogger(DiscordBot.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
                case "все": {
                    try {
                        db.updateVision(user.getId(), 3);
                        channel.sendMessage(user.getAsMention() + " Теперь вы видите скидки и новые товары").queue();
                    } catch (SQLException ex) {
                        Logger.getLogger(DiscordBot.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
                default:
                    channel.sendMessage(user.getAsMention() + " Не понимаю что вы хотите видеть\n"
                            + "Могу предложить вам видеть: \nтолько скидки \nтолько новое \nвсе товары").queue();
            }
        }

    }

    public void add(User user, String category) {
        if (userCategory.containsKey(category)) {
            userCategory.get(category).add(user);
        } else {
            ArrayList<User> list = new ArrayList<>();
            list.add(user);

            userCategory.put(category, list);
        }

    }

    public void add(String category) {

        ArrayList<User> list = new ArrayList<>();

        userCategory.put(category, list);

    }

    public void message(String category, String message, int vision) throws SQLException {
        List<Guild> guilds = jda.getGuilds();
        List<User> users = userCategory.get(category);
        if (users == null) {
            return;
        }

        if (users.isEmpty()) {
            return;
        }

        for (Guild guild : guilds) {
            if (guildsChannels.get(guild.getId()) == null) {
                continue;
            }
            TextChannel channel = guild.getTextChannelById(guildsChannels.get(guild.getId()));
            List<Member> members = channel.getMembers();
            List<String> useridVision = db.getUserVison(vision);
            StringBuilder mentionedUsers = new StringBuilder();
            for (Member member : members) {
                if (users.contains(member.getUser())) {
                    if (useridVision.contains(member.getId())) {
                        mentionedUsers.append(member.getUser().getAsMention()).append(" ");
                    }
                }
            }
            if (mentionedUsers.isEmpty()) {
                continue;
            }
            channel.sendMessage(mentionedUsers.toString() + message).queue();
        }
    }
}
