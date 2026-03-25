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

    static class BookingHistory {
        private List<Reservation> history = new ArrayList<>();

        public void addReservation(Reservation reservation) {
            history.add(reservation);
        }

        public List<Reservation> getAllReservations() {
            return history;
        }
    }

    static class BookingReportService {
        public void generateReport(List<Reservation> reservations) {
            System.out.println("\nBooking Report");

            if (reservations.isEmpty()) {
                System.out.println("No bookings found.");
                return;
            }

            for (Reservation r : reservations) {
                System.out.println(r.getReservationId() + " | " + r.getGuestName() + " | " + r.getRoomType());
            }

            System.out.println("Total Bookings: " + reservations.size());
        }
    }

    static class RoomAllocationService {
        private Map<String, Set<String>> assignedRoomsByType = new HashMap<>();
        private BookingHistory bookingHistory;

        public RoomAllocationService(BookingHistory bookingHistory) {
            this.bookingHistory = bookingHistory;
        }

        public void allocateRoom(Reservation reservation, RoomInventory inventory) {
            String roomType = reservation.getRoomType();
            int available = inventory.getAvailability(roomType);

            if (available > 0) {
                String roomId = generateRoomId(roomType);

                assignedRoomsByType.putIfAbsent(roomType, new HashSet<>());
                assignedRoomsByType.get(roomType).add(roomId);

                inventory.updateAvailability(roomType, available - 1);

                bookingHistory.addReservation(reservation);

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

    public static void main(String[] args) {

        BookingRequestQueue queue = new BookingRequestQueue();

        Reservation r1 = new Reservation("R101", "Abhi", "Single");
        Reservation r2 = new Reservation("R102", "Subha", "Single");
        Reservation r3 = new Reservation("R103", "Vanmathi", "Suite");

        queue.addRequest(r1);
        queue.addRequest(r2);
        queue.addRequest(r3);

        RoomInventory inventory = new RoomInventory();
        BookingHistory history = new BookingHistory();
        RoomAllocationService allocationService = new RoomAllocationService(history);
        BookingReportService reportService = new BookingReportService();

        while (queue.hasPendingRequests()) {
            Reservation r = queue.getNextRequest();
            allocationService.allocateRoom(r, inventory);
        }

        reportService.generateReport(history.getAllReservations());
    }
}