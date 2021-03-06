package commands;

import lib.CollectionManager;
import models.Ticket;
import server.Server;

import java.util.ArrayList;

public class Clear extends AbstractCommand {
    private final CollectionManager collectionManager;

    public Clear(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }


    @Override
    public ArrayList<String> execute(String argument, Ticket ticket, Integer id, String username) {
        ArrayList<String> clearCommand = new ArrayList<>();
        Server.getDatabase().clearCollection();
        clearCommand.add("collection cleaned successfully");
        return clearCommand;
    }

    @Override
    public String getDescription() {
        return " clear collection\n";
    }
}
