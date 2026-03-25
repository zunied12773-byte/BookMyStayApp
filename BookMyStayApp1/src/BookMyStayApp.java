import java.io.*;
import java.util.*;

public class BookMyStayApp {

    static class Reservation implements Serializable {
        private String reservationId;
        private String guestName;
        private String roomType;

        public Reservation(String reservationId, String guestName, String roomType) {
            this.reservationId = reservationId;
            this.guestName = guestName;
            this.roomType = roomType;
        }

        public String getReservationId() {
            return reservationId;
        }

        public String getGuestName() {
            return guestName;
        }

        public String getRoomType() {
            return roomType;
        }
    }

    static class RoomInventory implements Serializable {
        private Map<String, Integer> availability = new HashMap<>();

        public RoomInventory() {
            availability.put("Single", 2);
            availability.put("Double", 1);
            availability.put("Suite", 1);
        }

        public int getAvailability(String type) {
            return availability.getOrDefault(type, 0);
        }

        public void decrease(String type) {
            availability.put(type, getAvailability(type) - 1);
        }

        public void increase(String type) {
            availability.put(type, getAvailability(type) + 1);
        }

        public Map<String, Integer> getAll() {
            return availability;
        }

        public void setAll(Map<String, Integer> data) {
            availability = data;
        }
    }

    static class BookingHistory implements Serializable {
        private List<Reservation> history = new ArrayList<>();

        public void add(Reservation r) {
            history.add(r);
        }

        public List<Reservation> getAll() {
            return history;
        }

        public void setAll(List<Reservation> list) {
            history = list;
        }
    }

    static class RoomAllocationService {
        public void allocate(Reservation r, RoomInventory inventory, BookingHistory history) {
            if (inventory.getAvailability(r.getRoomType()) > 0) {
                inventory.decrease(r.getRoomType());
                history.add(r);
                System.out.println("Booked: " + r.getReservationId() + " | " + r.getGuestName());
            } else {
                System.out.println("No rooms for " + r.getGuestName());
            }
        }
    }

    static class PersistenceService {

        private static final String FILE = "data.ser";

        public static void save(RoomInventory inventory, BookingHistory history) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE))) {
                oos.writeObject(inventory);
                oos.writeObject(history);
                System.out.println("Data saved");
            } catch (Exception e) {
                System.out.println("Save failed");
            }
        }

        public static Object[] load() {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE))) {
                RoomInventory inventory = (RoomInventory) ois.readObject();
                BookingHistory history = (BookingHistory) ois.readObject();
                System.out.println("Data loaded");
                return new Object[]{inventory, history};
            } catch (Exception e) {
                System.out.println("No previous data found, starting fresh");
                return null;
            }
        }
    }

    public static void main(String[] args) {

        RoomInventory inventory;
        BookingHistory history;

        Object[] data = PersistenceService.load();

        if (data != null) {
            inventory = (RoomInventory) data[0];
            history = (BookingHistory) data[1];
        } else {
            inventory = new RoomInventory();
            history = new BookingHistory();
        }

        RoomAllocationService service = new RoomAllocationService();

        service.allocate(new Reservation("R101", "Abhi", "Single"), inventory, history);
        service.allocate(new Reservation("R102", "Subha", "Suite"), inventory, history);

        System.out.println("Total Bookings: " + history.getAll().size());

        PersistenceService.save(inventory, history);
    }
}