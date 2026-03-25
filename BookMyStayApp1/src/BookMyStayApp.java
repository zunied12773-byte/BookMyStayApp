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
        private Queue<Reservation> queue = new LinkedList<>();

        public synchronized void addRequest(Reservation r) {
            queue.offer(r);
        }

        public synchronized Reservation getNextRequest() {
            return queue.poll();
        }

        public synchronized boolean hasRequests() {
            return !queue.isEmpty();
        }
    }

    static class RoomInventory {
        private Map<String, Integer> availability = new HashMap<>();

        public RoomInventory() {
            availability.put("Single", 2);
            availability.put("Double", 1);
            availability.put("Suite", 1);
        }

        public synchronized int getAvailability(String type) {
            return availability.getOrDefault(type, 0);
        }

        public synchronized void decrease(String type) {
            availability.put(type, getAvailability(type) - 1);
        }
    }

    static class RoomAllocationService {
        private Map<String, Set<String>> assigned = new HashMap<>();

        public synchronized void allocateRoom(Reservation r, RoomInventory inventory) {
            String type = r.getRoomType();
            int available = inventory.getAvailability(type);

            if (available > 0) {
                String roomId = generateRoomId(type);

                assigned.putIfAbsent(type, new HashSet<>());
                assigned.get(type).add(roomId);

                inventory.decrease(type);

                System.out.println(Thread.currentThread().getName()
                        + " -> Booking confirmed: " + r.getGuestName()
                        + " | " + roomId);
            } else {
                System.out.println(Thread.currentThread().getName()
                        + " -> No rooms for " + r.getGuestName());
            }
        }

        private String generateRoomId(String type) {
            int count = assigned.getOrDefault(type, new HashSet<>()).size() + 1;
            return type + "-" + count;
        }
    }

    static class BookingProcessor extends Thread {
        private BookingRequestQueue queue;
        private RoomAllocationService service;
        private RoomInventory inventory;

        public BookingProcessor(String name,
                                BookingRequestQueue queue,
                                RoomAllocationService service,
                                RoomInventory inventory) {
            super(name);
            this.queue = queue;
            this.service = service;
            this.inventory = inventory;
        }

        public void run() {
            while (true) {
                Reservation r;

                synchronized (queue) {
                    if (!queue.hasRequests()) break;
                    r = queue.getNextRequest();
                }

                if (r != null) {
                    service.allocateRoom(r, inventory);
                }
            }
        }
    }

    public static void main(String[] args) {

        BookingRequestQueue queue = new BookingRequestQueue();

        queue.addRequest(new Reservation("R101", "Abhi", "Single"));
        queue.addRequest(new Reservation("R102", "Subha", "Single"));
        queue.addRequest(new Reservation("R103", "Rahul", "Single"));
        queue.addRequest(new Reservation("R104", "Sneha", "Suite"));

        RoomInventory inventory = new RoomInventory();
        RoomAllocationService service = new RoomAllocationService();

        BookingProcessor t1 = new BookingProcessor("Thread-1", queue, service, inventory);
        BookingProcessor t2 = new BookingProcessor("Thread-2", queue, service, inventory);

        t1.start();
        t2.start();
    }
}