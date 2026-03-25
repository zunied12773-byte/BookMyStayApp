import java.util.*;

public class BookMyStayApp {

    static class InvalidBookingException extends Exception {
        public InvalidBookingException(String message) {
            super(message);
        }
    }

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

        public boolean isValidRoomType(String roomType) {
            return roomAvailability.containsKey(roomType);
        }

        public void updateAvailability(String roomType, int count) throws InvalidBookingException {
            if (count < 0) {
                throw new InvalidBookingException("Invalid inventory state for room type: " + roomType);
            }
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

            for (Reservation r : reservations) {
                System.out.println(r.getReservationId() + " | " + r.getGuestName() + " | " + r.getRoomType());
            }

            System.out.println("Total Bookings: " + reservations.size());
        }
    }

    static class InvalidBookingValidator {

        public static void validate(Reservation reservation, RoomInventory inventory) throws InvalidBookingException {

            if (reservation.getGuestName() == null || reservation.getGuestName().isEmpty()) {
                throw new InvalidBookingException("Guest name cannot be empty");
            }

            if (!inventory.isValidRoomType(reservation.getRoomType())) {
                throw new InvalidBookingException("Invalid room type: " + reservation.getRoomType());
            }

            if (inventory.getAvailability(reservation.getRoomType()) <= 0) {
                throw new InvalidBookingException("No rooms available for type: " + reservation.getRoomType());
            }
        }
    }

    static class RoomAllocationService {
        private Map<String, Set<String>> assignedRoomsByType = new HashMap<>();
        private BookingHistory bookingHistory;

        public RoomAllocationService(BookingHistory bookingHistory) {
            this.bookingHistory = bookingHistory;
        }

        public void allocateRoom(Reservation reservation, RoomInventory inventory) {
            try {
                InvalidBookingValidator.validate(reservation, inventory);

                String roomType = reservation.getRoomType();
                int available = inventory.getAvailability(roomType);

                String roomId = generateRoomId(roomType);

                assignedRoomsByType.putIfAbsent(roomType, new HashSet<>());
                assignedRoomsByType.get(roomType).add(roomId);

                inventory.updateAvailability(roomType, available - 1);

                bookingHistory.addReservation(reservation);

                System.out.println("Booking confirmed: " + reservation.getGuestName()
                        + " | Room ID: " + roomId
                        + " | Reservation ID: " + reservation.getReservationId());

            } catch (InvalidBookingException e) {
                System.out.println("Booking failed: " + e.getMessage());
            }
        }

        private String generateRoomId(String roomType) {
            int count = assignedRoomsByType.getOrDefault(roomType, new HashSet<>()).size() + 1;
            return roomType + "-" + count;
        }
    }

    public static void main(String[] args) {

        BookingRequestQueue queue = new BookingRequestQueue();

        queue.addRequest(new Reservation("R101", "Abhi", "Single"));
        queue.addRequest(new Reservation("R102", "", "Double"));
        queue.addRequest(new Reservation("R103", "Rahul", "Deluxe"));
        queue.addRequest(new Reservation("R104", "Sneha", "Suite"));

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