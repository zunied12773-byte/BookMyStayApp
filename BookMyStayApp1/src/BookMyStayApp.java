import java.util.*;

public class BookMyStayApp {

    static class Reservation {
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

    static class BookingRequestQueue {
        private Queue<Reservation> requestQueue = new LinkedList<>();

        public void addRequest(Reservation reservation) {
            requestQueue.offer(reservation);
        }

        public Reservation getNextRequest() {
            return requestQueue.poll();
        }

        public boolean hasPendingRequests() {
            return !requestQueue.isEmpty();
        }
    }

    static class RoomInventory {
        private Map<String, Integer> roomAvailability = new HashMap<>();

        public RoomInventory() {
            roomAvailability.put("Single", 2);
            roomAvailability.put("Double", 1);
            roomAvailability.put("Suite", 1);
        }

        public int getAvailability(String roomType) {
            return roomAvailability.getOrDefault(roomType, 0);
        }

        public void updateAvailability(String roomType, int count) {
            roomAvailability.put(roomType, count);
        }
    }

    static class RoomAllocationService {
        private Map<String, Set<String>> assignedRoomsByType = new HashMap<>();

        public void allocateRoom(Reservation reservation, RoomInventory inventory) {
            String roomType = reservation.getRoomType();
            int available = inventory.getAvailability(roomType);

            if (available > 0) {
                String roomId = generateRoomId(roomType);

                assignedRoomsByType.putIfAbsent(roomType, new HashSet<>());
                assignedRoomsByType.get(roomType).add(roomId);

                inventory.updateAvailability(roomType, available - 1);

                System.out.println("Booking confirmed: " + reservation.getGuestName()
                        + " | Room ID: " + roomId
                        + " | Reservation ID: " + reservation.getReservationId());
            } else {
                System.out.println("No rooms available for " + reservation.getGuestName());
            }
        }

        private String generateRoomId(String roomType) {
            int count = assignedRoomsByType.getOrDefault(roomType, new HashSet<>()).size() + 1;
            return roomType + "-" + count;
        }
    }

    static class AddOnService {
        private String serviceName;
        private double cost;

        public AddOnService(String serviceName, double cost) {
            this.serviceName = serviceName;
            this.cost = cost;
        }

        public String getServiceName() {
            return serviceName;
        }

        public double getCost() {
            return cost;
        }
    }

    static class AddOnServiceManager {
        private Map<String, List<AddOnService>> serviceMap = new HashMap<>();

        public void addService(String reservationId, AddOnService service) {
            serviceMap.putIfAbsent(reservationId, new ArrayList<>());
            serviceMap.get(reservationId).add(service);
        }

        public double calculateTotalCost(String reservationId) {
            double total = 0;
            List<AddOnService> services = serviceMap.getOrDefault(reservationId, new ArrayList<>());
            for (AddOnService s : services) {
                total += s.getCost();
            }
            return total;
        }

        public void displayServices(String reservationId) {
            List<AddOnService> services = serviceMap.getOrDefault(reservationId, new ArrayList<>());

            System.out.println("\nServices for Reservation ID: " + reservationId);

            if (services.isEmpty()) {
                System.out.println("No add-on services.");
                return;
            }

            for (AddOnService s : services) {
                System.out.println(s.getServiceName() + " - ₹" + s.getCost());
            }

            System.out.println("Total Add-On Cost: ₹" + calculateTotalCost(reservationId));
        }
    }

    public static void main(String[] args) {

        BookingRequestQueue queue = new BookingRequestQueue();

        Reservation r1 = new Reservation("R101", "Abhi", "Single");
        Reservation r2 = new Reservation("R102", "Subha", "Single");

        queue.addRequest(r1);
        queue.addRequest(r2);

        RoomInventory inventory = new RoomInventory();
        RoomAllocationService allocationService = new RoomAllocationService();

        while (queue.hasPendingRequests()) {
            Reservation r = queue.getNextRequest();
            allocationService.allocateRoom(r, inventory);
        }

        AddOnServiceManager manager = new AddOnServiceManager();

        manager.addService("R101", new AddOnService("Breakfast", 200));
        manager.addService("R101", new AddOnService("Airport Pickup", 500));
        manager.addService("R102", new AddOnService("Extra Bed", 300));

        manager.displayServices("R101");
        manager.displayServices("R102");
    }
}