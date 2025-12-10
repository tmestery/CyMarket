package onetomany.BackendTests;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.HashSet;
import java.util.Optional; // Ensure this is imported
import onetomany.Items.ItemImage;
import java.util.Collections;
import java.util.Map;
import java.util.Base64;
import org.springframework.mock.web.MockMultipartFile;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.mockito.ArgumentMatchers.anyLong;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import onetomany.Items.Item;
import onetomany.Items.ItemImageRepository;
import onetomany.Items.ItemsController;
import onetomany.Items.ItemsRepository;
import onetomany.Users.PasswordRecoveryService;
import onetomany.Users.User;
import onetomany.Users.UserController;
import onetomany.Users.UserImageRepository;
import onetomany.Users.UserRepository;
import onetomany.userLogIn.userLogin;
import onetomany.userLogIn.userLoginRepository;
import onetomany.Sellers.SellerRepository;
import onetomany.Sellers.Seller;
import onetomany.Checkout.CheckoutRepository;
import onetomany.Notifications.NotificationService;
import onetomany.Notifications.Notification;
import onetomany.Notifications.NotificationType;
import onetomany.Checkout.Checkout;
import onetomany.Checkout.OrderStatus;
import onetomany.Checkout.CheckoutItem;
import onetomany.Checkout.CheckoutService;
import onetomany.Checkout.CheckoutRequest;
import onetomany.Checkout.CheckoutItemRequest;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {UserController.class, ItemsController.class})
public class JasonSystemTest {

    @Autowired
    private MockMvc controller;


    @MockBean private UserRepository userRepository;
    @MockBean private userLoginRepository userLoginRepo;
    @MockBean private PasswordRecoveryService passwordRecoveryService;
    @MockBean private UserImageRepository userImageRepository;
    @MockBean private ItemsRepository itemsRepository;
    @MockBean private SellerRepository sellerRepository;
    @MockBean private ItemImageRepository itemImageRepository;
    // Added mocks for Checkout and Notification coverage
    @MockBean private CheckoutRepository checkoutRepository;
    @MockBean private NotificationService notificationService;

    @Test
    public void testCreateAndFetchUser() throws Exception {
        List<User> userList = new ArrayList<>();

        when(userRepository.save(any(User.class))).thenAnswer(x -> {
            User r = x.getArgument(0);
            if(r.getId() == 0) r.setId(1);
            userList.add(r);
            return r;
        });

        when(userLoginRepo.save(any(userLogin.class))).thenReturn(null);

        when(userRepository.findUserByUsername(any(String.class))).thenAnswer(x -> {
             String name = x.getArgument(0);
             for (User u : userList) {
                 if (u.getUsername() != null && u.getUsername().equals(name)) return u;
             }
             return null;
        });

        when(userRepository.findById(anyInt())).thenAnswer(x -> {
            int id = x.getArgument(0);
            for (User u : userList) {
                if (u.getId() == id) return u;
            }
            return null;
        });

        String jsonUser = "{\"username\":\"testUser\", \"emailId\":\"test@example.com\", \"userPassword\":\"password123\"}";

        controller.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("success")));

            controller.perform(get("/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username", is("testUser")));
    }


    @Test
    public void testEntitiesAndCheckoutServiceCoverage() {
        // --- 1. User Entity Coverage ---
        User user = new User("Coverage User", "cov@test.com", "pass", "covUser");
        user.setId(1);
        user.setBalance(500.0);
        user.setIfActive(true);
        user.setJoiningDate(new Date());
        user.setLastLoggin();
        user.setPasswordRecoveryCode("1234");
        user.setPasswordRecoveryExpiry(new Date());

        // Validate basic getters
        assert user.getId() == 1;
        assert user.getBalance() == 500.0;
        assert user.getName().equals("Coverage User");
        assert user.getPasswordRecoveryCode().equals("1234");
        assert user.getUserGroups() != null;
        
        // Test Item liking in User
        Item item = new Item();
        item.setId(101); // Item ID is int
        item.setName("Test Item");
        item.setPrice(50.0);
        item.setQuantity(10);
        item.setIfAvailable(true);
        
        // Item getters coverage
        assert item.getId() == 101;
        assert item.getName().equals("Test Item");
        assert item.getPrice() == 50.0;
        assert item.getQuantity() == 10;
        assert item.isIfAvailable();

        user.addItem(item);
        assert user.getLikedItems().contains(item);
        assert user.getLikedItemsCount() == 1;
        user.removeItem(item);
        assert user.getLikedItemsCount() == 0;
        user.setLikedItems(new HashSet<>());
        user.addItem(item);
        user.removeAllItems();
        assert user.getLikedItems().isEmpty();

        // Coverage for User.getNotifications
        assert user.getNotifications() == null || user.getNotifications().isEmpty();

        // --- 2. Notification Entity Coverage ---
        Notification notif = new Notification(user, NotificationType.TRANSACTION_PENDING, "Test Message");
        notif.setId(1L);
        notif.setRead(true);
        notif.setRelatedEntityId(55L);
        notif.setRelatedEntityType("ORDER");
        notif.setActionUrl("/orders/55");
        notif.setCreatedAt(new Date());
        
        assert notif.getId() == 1L;
        assert notif.isRead();
        assert notif.getType() == NotificationType.TRANSACTION_PENDING;
        assert notif.getActionUrl().equals("/orders/55");
        assert notif.getUser() == user;
        
        // Constructor coverage
        Notification notif2 = new Notification(user, NotificationType.SYSTEM_ANNOUNCEMENT, "Msg", 1L, "ITEM");
        assert notif2.getRelatedEntityType().equals("ITEM");

        // --- 3. Checkout Entity Coverage ---
        Checkout checkout = new Checkout(user, "123 Main St", "Ames", "IA", "50010", "USA");
        checkout.setBillingAddress("123 Main St");
        checkout.setBillingCity("Ames");
        checkout.setBillingState("IA");
        checkout.setBillingZipCode("50010");
        checkout.setBillingCountry("USA");
        checkout.setNotes("Leave at door");
        checkout.setCardInfo("1234567812345678", "Test Holder");

        assert checkout.getCardLastFour().equals("5678");
        assert checkout.getCardHolderName().equals("Test Holder");
        assert checkout.getShippingZipCode().equals("50010");
        
        // Coverage for Checkout Default Constructor
        Checkout emptyCheckout = new Checkout();
        assert emptyCheckout.getStatus() == OrderStatus.PENDING;
        assert emptyCheckout.getOrderDate() != null;

        // Checkout Item Coverage
        Seller seller = new Seller();
        seller.setId(5L);
        seller.setTotalSales(100);
        // Mock seller login for notifications
        userLogin sellerLogin = new userLogin();
        User sellerUser = new User();
        sellerUser.setId(2);
        sellerUser.setBalance(0.0);
        sellerLogin.setUser(sellerUser);
        seller.setUserLogin(sellerLogin);
        item.setSeller(seller);

        // Seller getters coverage
        assert seller.getId() == 5L;
        assert seller.getTotalSales() == 100;
        assert seller.getUserLogin() == sellerLogin;

        CheckoutItem checkoutItem = new CheckoutItem(item, 2, seller);
        checkoutItem.setId(99L);
        assert checkoutItem.getPrice() == 50.0; // Should match item price
        // FIXED: getTotalPrice() method doesn't exist, calculating manually
        assert (checkoutItem.getPrice() * checkoutItem.getQuantity()) == 100.0;
        
        checkout.addItem(checkoutItem);
        assert checkout.getItems().size() == 1;
        assert checkout.getTotalPrice() == 100.0;
        
        checkout.removeItem(checkoutItem);
        assert checkout.getItems().isEmpty();
        
        checkout.addItem(checkoutItem);
        checkout.completeOrder();
        assert checkout.getStatus() == OrderStatus.COMPLETED;
        
        // Coverage for cancelOrder and getItemCount
        checkout.cancelOrder();
        assert checkout.getStatus() == OrderStatus.CANCELLED;

        // getItemCount sums quantities (checkoutItem quantity is 2)
        assert checkout.getItemCount() == 2;

        // --- 4. CheckoutService Logic Coverage ---
        // We manually instantiate the service and inject mocks to test the logic
        CheckoutService service = new CheckoutService();
        ReflectionTestUtils.setField(service, "checkoutRepository", checkoutRepository);
        ReflectionTestUtils.setField(service, "userRepository", userRepository);
        ReflectionTestUtils.setField(service, "itemsRepository", itemsRepository);
        ReflectionTestUtils.setField(service, "sellerRepository", sellerRepository);
        ReflectionTestUtils.setField(service, "notificationService", notificationService);

        // Setup Request
        CheckoutRequest req = new CheckoutRequest();
        // FIXED: Casting int ID to long for setUserId
        req.setUserId((int) user.getId());
        req.setShippingAddress("Addr"); req.setShippingCity("City"); req.setShippingState("ST"); req.setShippingZipCode("00000"); req.setShippingCountry("US");
        req.setBillingAddress("Addr"); req.setBillingCity("City"); req.setBillingState("ST"); req.setBillingZipCode("00000"); req.setBillingCountry("US");
        req.setCardNumber("1234567890123456");
        req.setCardHolderName("Holder");
        req.setExpirationMonth("12");
        req.setExpirationYear("2030");
        req.setCvv("123");
        
        List<CheckoutItemRequest> reqItems = new ArrayList<>();
        CheckoutItemRequest itemReq = new CheckoutItemRequest();
        // FIXED: Cast int ID to Long if setItemId expects Long, or ensure it matches
        // Based on error "int cannot be converted to Long", setItemId likely takes Long
        itemReq.setItemId((long) item.getId()); 
        itemReq.setQuantity(1);
        reqItems.add(itemReq);
        req.setItems(reqItems);

        // Define Mock Behaviors for Service call
        when(userRepository.findById(user.getId())).thenReturn(user);

        // FIXED: Use any() to match both int and Integer, and return Optional because service expects it
        when(itemsRepository.findById(any())).thenReturn(Optional.of(item));
        
        when(checkoutRepository.save(any(Checkout.class))).thenAnswer(invocation -> {
                Checkout c = invocation.getArgument(0);
            c.setId(777L);
            return c;
        });

        // Execute Service Method
        Checkout resultOrder = service.createCheckout(req);

        // Verify Results
        assert resultOrder != null;
        assert resultOrder.getId() == 777L;
        assert user.getBalance() == 450.0; // 500 - 50
        assert item.getQuantity() == 9; // 10 - 1
    
        // Verify Notification interaction
        verify(notificationService, atLeastOnce()).createAndSendNotification(
            any(User.class), 
            any(NotificationType.class), 
            anyString(), 
            any(), 
            anyString(), 
            any()
        );
    }

    @Test
    public void testCreateItem() throws Exception {
        List<Item> itemList = new ArrayList<>();

        when(itemsRepository.save(any(Item.class))).thenAnswer(x -> {
            Item i = x.getArgument(0);
            if(i.getId() == 0) i.setId(100);
            itemList.add(i);
            return i;
        });
        
        when(itemsRepository.findAll()).thenReturn(itemList);
        when(itemsRepository.findById(anyInt())).thenReturn(new Item());

        String jsonItem = "{\"name\":\"Gaming Console\", \"description\":\"New\", \"price\":499.99}";

        controller.perform(post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonItem))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("success")));

        System.out.println("Items count: " + itemList.size());
    }

    @Test
    public void testItemControllerAdditionalMethods() throws Exception {
        // Setup Item and Seller
        Item item = new Item();
        item.setId(200);
        item.setName("Specific Item");
        item.setQuantity(5);
        item.setPrice(10.0);
        
        Seller seller = new Seller();
        seller.setId(50L);
        seller.setUsername("SellerUser");
        seller.setTotalSales(10);
        item.setSeller(seller);

        // Mocks
        when(itemsRepository.findById(200)).thenReturn(item);
        when(itemsRepository.findByUsername("Specific Item")).thenReturn(item); // Mock for getUser/findByUsername
        when(sellerRepository.findById(50L)).thenReturn(seller);
        when(itemsRepository.save(any(Item.class))).thenAnswer(i -> i.getArgument(0));

        // 1. getAllItems
        List<Item> allItems = new ArrayList<>();
        allItems.add(item);
        when(itemsRepository.findAll()).thenReturn(allItems);
        controller.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Specific Item")));

        // 2. getAllUser (findById)
        controller.perform(get("/items/200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Specific Item")));

        // 3. getUser (findByUsername)
        controller.perform(get("/items/u/Specific Item"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Specific Item")));

        // 4. createItemWithSeller
        String newItemJson = "{\"name\":\"Seller Item\", \"price\":20.0}";
        controller.perform(post("/items/seller/50")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newItemJson))
                .andExpect(status().isCreated());

        // 5. updateItem
        String updateJson = "{\"name\":\"Updated Item\", \"price\":15.0}";
        controller.perform(put("/items/200")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());

        // 6. deleteItem
        controller.perform(delete("/items/200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("success")));

        // 7. getItemSeller
        controller.perform(get("/items/200/seller"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("SellerUser")));

        // 8. createItemForSeller
        controller.perform(post("/items/50/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newItemJson))
                .andExpect(status().isCreated());

        // 9. getItemQuantity
        controller.perform(get("/items/200/quantity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(5)));

        // 10. updateItemQuantity
        controller.perform(put("/items/200/quantity?quantity=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(10)));
        // Reset quantity for next tests
        item.setQuantity(5);

        // 11. incrementItemQuantity
        controller.perform(post("/items/200/quantity/increment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(6)));

        // 12. decrementItemQuantity
        controller.perform(post("/items/200/quantity/decrement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(5)));

        // 13. Image Handling
        MockMultipartFile file = new MockMultipartFile("images", "test.jpg", "image/jpeg", "some xml".getBytes());
        
        // uploadItemImages
        controller.perform(multipart("/items/200/images").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Images uploaded successfully"));

        // Mock Image retrieval
        ItemImage itemImage = new ItemImage("some xml".getBytes(), "image/jpeg");
        // itemImage.setId(1L);
        itemImage.setItem(item);
        item.addImage(itemImage); // Add directly to item for getImages test

        when(itemImageRepository.findById(1L)).thenReturn(Optional.of(itemImage));

        // getItemImages
        controller.perform(get("/items/200/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].contentType", is("image/jpeg")));

        // getItemImage (single)
        controller.perform(get("/items/200/images/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));

        // deleteItemImage
        controller.perform(delete("/items/200/images/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Image deleted successfully"));
    }
    

    @Test
    public void testDeleteUser() throws Exception {
        List<User> userList = new ArrayList<>();
        User u = new User();
        u.setId(1);
        u.setUsername("deleteMe");
        userList.add(u);

        when(userRepository.findById(1)).thenReturn(u);

        doAnswer(x -> {
            userList.remove(u);
            return null;
        }).when(userRepository).delete(u);

        controller.perform(delete("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("success")));
                
        if (userList.isEmpty()) {
            System.out.println("User deleted successfully in mock.");
        }
    }
}
