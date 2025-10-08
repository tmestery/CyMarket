package onetomany.Sellers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import onetomany.Items.Item;
import onetomany.Items.ItemsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Validated
@RestController
@RequestMapping("/sellers")
public class SellerController {

    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private ItemsRepository itemsRepository;

    // GET all sellers
    @GetMapping
    public List<Seller> getAll() {
        return sellerRepository.findAll();
    }

    // GET seller by ID
    @GetMapping("/{id}")
    public Seller getById(@PathVariable long id) {
        Seller seller = sellerRepository.findById(id);
        if (seller == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found");
        }
        return seller;
    }

    // GET seller by username
    @GetMapping("/u/{username}")
    public Seller getByUsername(@PathVariable String username) {
        Seller seller = sellerRepository.findByUsername(username);
        if (seller == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found");
        }
        return seller;
    }

    // Check if username exists
    @GetMapping("/exists")
    public Map<String, Boolean> usernameExists(@RequestParam("username") String username) {
        return Map.of("exists", sellerRepository.existsByUsername(username));
    }

    // CREATE new seller
    @PostMapping
    public ResponseEntity<Seller> create(@Valid @RequestBody Seller seller) {
        if (sellerRepository.existsByUsername(seller.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        // Set defaults for null fields
        if (seller.getCreatedAt() == null) {
            seller.setCreatedAt(new Date());
        }
        if (seller.getActive() == null) {
            seller.setActive(true);
        }

        Seller saved = sellerRepository.save(seller);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // UPDATE seller
    @PutMapping("/{id}")
    public ResponseEntity<Seller> update(@PathVariable long id, @Valid @RequestBody Seller incoming) {
        Seller current = sellerRepository.findById(id);
        if (current == null) {
            return ResponseEntity.notFound().build();
        }

        // Check for username conflict
        if (!incoming.getUsername().equals(current.getUsername())
                && sellerRepository.existsByUsername(incoming.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Update fields
        current.setUsername(incoming.getUsername());
        current.setBio(incoming.getBio());
        current.setActive(incoming.getActive() != null ? incoming.getActive() : current.getActive());
        current.setRating(incoming.getRating() != null ? incoming.getRating() : current.getRating());
        current.setRatingsCount(incoming.getRatingsCount() != null ? incoming.getRatingsCount() : current.getRatingsCount());
        current.setTotalSales(incoming.getTotalSales() != null ? incoming.getTotalSales() : current.getTotalSales());

        if (incoming.getCreatedAt() != null) {
            current.setCreatedAt(incoming.getCreatedAt());
        }

        return ResponseEntity.ok(sellerRepository.save(current));
    }

    // DELETE seller
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!sellerRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        sellerRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Rate seller
    @PostMapping("/{id}/rate")
    public ResponseEntity<Seller> addRating(
            @PathVariable long id,
            @DecimalMin("0.0") @DecimalMax("5.0") @RequestParam double value) {
        Seller seller = sellerRepository.findById(id);
        if (seller == null) {
            return ResponseEntity.notFound().build();
        }

        // if null initialize
        if (seller.getRatingsCount() == null) {
            seller.setRatingsCount(0);
        }
        if (seller.getRating() == null) {
            seller.setRating(0.0);
        }

        // Calculate average rating
        double totalRating = seller.getRating() * seller.getRatingsCount() + value;
        seller.setRatingsCount(seller.getRatingsCount() + 1);
        seller.setRating(totalRating / seller.getRatingsCount());

        return ResponseEntity.ok(sellerRepository.save(seller));
    }

    // Increment sale count
    @PostMapping("/{id}/sale")
    public ResponseEntity<Seller> incrementSale(@PathVariable long id) {
        Seller seller = sellerRepository.findById(id);
        if (seller == null) {
            return ResponseEntity.notFound().build();
        }

        if (seller.getTotalSales() == null) {
            seller.setTotalSales(0);
        }
        seller.setTotalSales(seller.getTotalSales() + 1);

        return ResponseEntity.ok(sellerRepository.save(seller));
    }

    // GET items for a seller
    @GetMapping("/{id}/items")
    public ResponseEntity<Set<Item>> getSellerItems(@PathVariable long id) {
        Seller seller = sellerRepository.findById(id);
        if (seller == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(seller.getItems());
    }

    //GET items count for a seller
    @GetMapping("/{id}/items/count")
    public ResponseEntity<Map<String, Integer>> getSellerItemsCount(@PathVariable long id) {
        Seller seller = sellerRepository.findById(id);
        if (seller == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("count", seller.getItemsCount()));
    }

    // POST seller creates item
    @PostMapping("/{id}/items")
    public ResponseEntity<Item> createItemForSeller(@PathVariable long id, @RequestBody Item item) {
        Seller seller = sellerRepository.findById(id);
        if (seller == null) {
            return ResponseEntity.notFound().build();
        }

        if (item.getCreationDate() == null) {
            item.setCreationDate(new Date());
        }
        item.setIfAvailable(true);

        seller.addItem(item);

        sellerRepository.save(seller);
        item.setSeller(seller);
        Item savedItem = itemsRepository.save(item);
        seller.addItem(savedItem);
        sellerRepository.save(seller);
        

        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    // DELETE seller deletes item
    @DeleteMapping("/{sellerId}/items/{itemId}")
    public ResponseEntity<Void> deleteSellerItem(@PathVariable long sellerId, @PathVariable int itemId) {
        Seller seller = sellerRepository.findById(sellerId);
        if (seller == null) {
            return ResponseEntity.notFound().build();
        }

        Item item = itemsRepository.findById(itemId);
        if (item == null || !item.getSellerId().equals(sellerId)) {
            return ResponseEntity.notFound().build();
        }

        seller.removeItem(item);
        itemsRepository.delete(item);

        return ResponseEntity.noContent().build();
    }
}
