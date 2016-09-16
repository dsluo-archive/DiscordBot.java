package commands.meme;

import com.github.alphahelix00.discordinator.d4j.commands.utils.CommandUtils;
import com.github.alphahelix00.ordinator.commands.MainCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.MessageBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

/**
 * Created by David on 9/15/2016.
 */
public class MemeCommands {

    MemeDatabase database;

    private Logger LOGGER = LoggerFactory.getLogger(MemeCommands.class);

    public MemeCommands() {
        database = MemeDatabase.getMemeDatabase();
    }

    @MainCommand(
            prefix = "?",
            name = "Meme",
            alias = {"meme"},
            description = "Get a random dank may may",
            usage = "?meme"
    )
    public void meme(List<String> args, MessageReceivedEvent event, MessageBuilder builder) {
        Meme meme = database.loadRandomMeme();

        String message = (meme != null) ? meme.toString() : "No memes in database. Add some with ?addmeme <name of meme> <link to meme>";

        CommandUtils.messageRequestBuffer(message, event, builder, "Meme");
    }

    @MainCommand(
            prefix = "?",
            name = "Add Meme",
            alias = {"addmeme"},
            description = "Add a meme to the database",
            usage = "?addmeme <name of meme> <link to meme>"
    )
    public void addMeme(List<String> args, MessageReceivedEvent event, MessageBuilder builder) {

        String name;
        String link;
        String owner;
        String message = "";

        URL          url;
        final String SUCCESS;
        final String INVALID_URL;
        final String INVALID_SYNTAX = "Invalid syntax.";

        final String[] embeddableExtensions = {
                "png",
                "jpeg",
                "jpg",
                "gifv",
                "gif"
//                "mp4",
        };

        final String[] embeddableWebsites = {
                "imgur.com",
                "youtube.com",
                "vimeo.com",
                "soundcloud.com",
                "twitch.tv"
        };

//        not perfect, but works well enough, i think
        Function<URL, Boolean> isEmbeddable = s -> {
            for (String host : embeddableWebsites)
                if (s.getHost().equals(host))
                    return true;

            for (String ext : embeddableExtensions)
                if (s.getPath().endsWith(ext))
                    return true;

            return false;
        };

        if (args.size() >= 2) {
            SUCCESS = "Successfully added meme **" + args.get(0) + "**.";
            INVALID_URL = "Invalid URL: " + args.get(1) + ".";

            name = args.get(0);
            link = args.get(1);
            owner = event.getMessage().getAuthor().getName();

            while (message.equals("")) {
                try {
                    url = new URL(link);

                    if (isEmbeddable.apply(url)) {

                        database.addMeme(new Meme(name, link, owner));

                        message = SUCCESS;

                    } else {
                        message = INVALID_URL;
                    }

                } catch (MalformedURLException e) {
                    if (e.getMessage().substring(0, 12).equals("no protocol: ")) {
                        link = "http://" + link;
                    } else {
                        message = INVALID_URL;
                    }
                }
            }
        } else {
            message = INVALID_SYNTAX;
        }

        CommandUtils.messageRequestBuffer(message, event, builder, "Add Meme");
    }

//    @MainCommand(
//            prefix = "?",
//            name = "Delete Meme",
//            alias = {"delmeme", "deletememe"},
//            description = "Delete a meme"
//    )

    @MainCommand(
            prefix = "?",
            name = "test command",
            alias = {"test"},
            description = "test command"
    )
    public void test(List<String> args, MessageReceivedEvent event, MessageBuilder builder) {
        String message = "";
        URL    url;

        try {
            url = new URL(args.get(0));
            message = url.getFile();

        } catch (IOException e) {
            message = e.getMessage();
        }

        CommandUtils.messageRequestBuffer(message, event, builder, "test command");
    }

    @MainCommand(
            prefix = "?",
            name = "List Memes",
            alias = {"listmemes", "memelist"},
            description = "List all memes"
    )
    public void listMemes(List<String> args, MessageReceivedEvent event, MessageBuilder builder) {

        String message;

        MonospaceTable table = new MonospaceTable("ID", "Name", "Owner", "Date Added", "Link");

        for (Meme meme : database.getMemeArray())
            table.add(String.valueOf(meme.getId()), meme.getName(), meme.getOwner(), String.valueOf(meme.getTimestamp()), meme.getLink());

        message = "```" + table.toString() + "```";

        CommandUtils.messageRequestBuffer(message, event, builder, "List Memes");
    }
}