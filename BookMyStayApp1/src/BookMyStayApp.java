import java.util.*;

public class BookMyStayApp {

    static class Reservation {
        private String guestName;
        private String roomType;

        public Reservation(String guestName, String roomType) {
            this.guestName = guestName;
            this.roomType = roomType;
        }

        public String getGuestName() {
            return guestName;
        }

        public String getRoomType() {
            return roomType;
        }
    }

    static class BookingRequestQueue {
        private Queue<Reservation> requestQueue;

        public BookingRequestQueue() {
            requestQueue = new LinkedList<>();
        }

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
        private Map<String, Integer> roomAvailability;

        public RoomInventory() {
            roomAvailability = new HashMap<>();
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
        private Set<String> allocatedRoomIds;
        private Map<String, Set<String>> assignedRoomsByType;

        public RoomAllocationService() {
            allocatedRoomIds = new HashSet<>();
            assignedRoomsByType = new HashMap<>();
        }

        public void allocateRoom(Reservation reservation, RoomInventory inventory) {
            String roomType = reservation.getRoomType();
            int available = inventory.getAvailability(roomType);

            if (available > 0) {
                String roomId = generateRoomId(roomType);

                allocatedRoomIds.add(roomId);

                assignedRoomsByType.putIfAbsent(roomType, new HashSet<>());
                assignedRoomsByType.get(roomType).add(roomId);

                inventory.updateAvailability(roomType, available - 1);

                System.out.println("Booking confirmed for Guest: " + reservation.getGuestName() + ", Room ID: " + roomId);
            } else {
                System.out.println("No rooms available for Guest: " + reservation.getGuestName());
            }
        }

        private String generateRoomId(String roomType) {
            int count = assignedRoomsByType.getOrDefault(roomType, new HashSet<>()).size() + 1;
            return roomType + "-" + count;
        }
    }

    public static void main(String[] args) {

        System.out.println("Room Allocation Processing");

        BookingRequestQueue queue = new BookingRequestQueue();

        queue.addRequest(new Reservation("Abhi", "Single"));
        queue.addRequest(new Reservation("Subha", "Single"));
        queue.addRequest(new Reservation("Vanmathi", "Suite"));

        RoomInventory inventory = new RoomInventory();
        RoomAllocationService allocationService = new RoomAllocationService();

        while (queue.hasPendingRequests()) {
            Reservation r = queue.getNextRequest();
            allocationService.allocateRoom(r, inventory);
        }
    }
}