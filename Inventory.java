import java.util.ArrayList;
import java.util.List;

public class Inventory {

    // Static list to hold inventory items as strings
    private static List<String> items = new ArrayList<>();

    // Static initializer block to add some starting items to the inventory
    static {
        items.add("Phone - Useless right now with no connection");   // Example item 1
        items.add("Wallet - Not even sure if there is a currency here at all"); // Example item 2
        items.add("Handkerchief - Mom's trusty good luck charm");    // Example item 3
    }

     // Adds an item to the inventory.
     // item The string description of the item to add
    public static void addItem(String item) {
        items.add(item);
    }

    
     // Returns a copy of the current list of items in the inventory.
     // Returning a new ArrayList prevents external modification of the original list.
     // Returns list of inventory item strings
     
    public static List<String> getItems() {
        return new ArrayList<>(items);
    }

    
     // Returns a formatted string representation of the inventory,
     // listing all items with bullet points.
     // Useful for displaying inventory contents in UI or console.
     // Formatted inventory string
    public static String getFormattedInventory() {
        StringBuilder sb = new StringBuilder("Inventory:\n");
        for (String item : items) {
            sb.append("- ").append(item).append("\n"); // Append each item on a new line with a dash
        }
        return sb.toString();
    }
}
