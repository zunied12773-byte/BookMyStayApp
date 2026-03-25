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

        public void increaseAvailability(String roomType) {
            roomAvailability.put(roomType, getAvailability(roomType) + 1);
        }

        public void decreaseAvailability(String roomType) {
            roomAvailability.put(roomType, getAvailability(roomType) - 1);
        }
    }

    static class BookingHistory {
        private Map<String, Reservation> history = new HashMap<>();

        public void addReservation(Reservation reservation) {
            history.put(reservation.getReservationId(), reservation);
        }

        public Reservation getReservation(String reservationId) {
            return history.get(reservationId);
        }

        public void removeReservation(String reservationId) {
            history.remove(reservationId);
        }

        public Collection<Reservation> getAllReservations() {
            return history.values();
        }
    }

    static class RoomAllocationService {
        private Map<String, String> reservationToRoom = new HashMap<>();
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

                reservationToRoom.put(reservation.getReservationId(), roomId);

                inventory.decreaseAvailability(roomType);

                bookingHistory.addReservation(reservation);

                System.out.println("Booking confirmed: " + reservation.getGuestName()
                        + " | Room ID: " + roomId
                        + " | Reservation ID: " + reservation.getReservationId());
            } else {
                System.out.println("No rooms available for " + reservation.getGuestName());
            }
        }

        public String getRoomId(String reservationId) {
            return reservationToRoom.get(reservationId);
        }

        public void removeAllocation(String reservationId) {
            reservationToRoom.remove(reservationId);
        }

        private String generateRoomId(String roomType) {
            int count = assignedRoomsByType.getOrDefault(roomType, new HashSet<>()).size() + 1;
            return roomType + "-" + count;
        }
    }

    static class CancellationService {
        private Stack<String> rollbackStack = new Stack<>();
        private RoomAllocationService allocationService;
        private BookingHistory bookingHistory;
        private RoomInventory inventory;

        public CancellationService(RoomAllocationService allocationService,
                                   BookingHistory bookingHistory,
                                   RoomInventory inventory) {
            this.allocationService = allocationService;
            this.bookingHistory = bookingHistory;
            this.inventory = inventory;
        }

        public void cancelBooking(String reservationId) {

            Reservation reservation = bookingHistory.getReservation(reservationId);

            if (reservation == null) {
                System.out.println("Cancellation failed: Reservation not found");
                return;
            }

            String roomId = allocationService.getRoomId(reservationId);

            if (roomId == null) {
                System.out.println("Cancellation failed: Already cancelled");
                return;
            }

            rollbackStack.push(roomId);

            inventory.increaseAvailability(reservation.getRoomType());

            allocationService.removeAllocation(reservationId);
            bookingHistory.removeReservation(reservationId);

            System.out.println("Booking cancelled: " + reservationId + " | Room released: " + roomId);
        }

        public void showRollbackStack() {
            System.out.println("\nRollback Stack: " + rollbackStack);
        }
    }

    public static void main(String[] args) {

        BookingRequestQueue queue = new BookingRequestQueue();

        Reservation r1 = new Reservation("R101", "Abhi", "Single");
        Reservation r2 = new Reservation("R102", "Subha", "Single");

        queue.addRequest(r1);
        queue.addRequest(r2);

        RoomInventory inventory = new RoomInventory();
        BookingHistory history = new BookingHistory();
        RoomAllocationService allocationService = new RoomAllocationService(history);
        CancellationService cancellationService = new CancellationService(allocationService, history, inventory);

        while (queue.hasPendingRequests()) {
            Reservation r = queue.getNextRequest();
            allocationService.allocateRoom(r, inventory);
        }

        cancellationService.cancelBooking("R101");
        cancellationService.cancelBooking("R999");

        cancellationService.showRollbackStack();
    }
}