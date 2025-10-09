package onetomany.Items;

import onetomany.Sellers.Seller;
import onetomany.Sellers.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Daniel Pinilla
 *
 */

@RestController
@RequestMapping("/items")
public class ItemsController {

    @Autowired
    ItemsRepository itemsRepository;
    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private ItemImageRepository itemImageRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    @GetMapping  // Remove "/items" - already in @RequestMapping
    List<Item> getAllItems(){
       
        return itemsRepository.findAll();
    }
    @GetMapping("/{id}")  // Remove "/items" prefix
    Item getAllUser(@PathVariable int id){
        return  itemsRepository.findById(id);
    }

    @GetMapping("/u/{username}")  // Remove "/items" prefix
    Item getUser (@PathVariable String username){
      return itemsRepository.findByUsername(username);
    }

    @PostMapping  // Remove "/items" prefix
    String createItem(@RequestBody Item item){
        if (item == null)
            return failure;
        item.setCreationDate(new Date());
        item.setIfAvailable(true);
        itemsRepository.save(item);

        return success;
    }

    // POST create item for a seller
    @PostMapping(path = "/seller/{sellerId}")
    ResponseEntity<Item> createItemWithSeller(@PathVariable long sellerId, @RequestBody Item item) {
        Seller seller = sellerRepository.findById(sellerId);
        if (seller == null) {
            return ResponseEntity.notFound().build();
        }

        if (item == null) {
            return ResponseEntity.badRequest().build();
        }

        item.setCreationDate(new Date());
        item.setIfAvailable(true);
        seller.addItem(item);
        Item savedItem = itemsRepository.save(item);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    @PutMapping("/{id}")
    Item updateItem(@PathVariable int id, @RequestBody Item request) {
        Item item = itemsRepository.findById(id);

        if (item == null)
            return null;
        itemsRepository.save(request);
        return itemsRepository.findById(id);
    }


    @DeleteMapping(path = "/{id}")
    String deleteItem(@PathVariable int id) {
        Item temp = itemsRepository.findById(id);
        if (temp == null)
            return failure;

        itemsRepository.delete(temp);

        return success;
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<String> uploadItemImages(@PathVariable int id, @RequestParam("images") List<MultipartFile> imageFiles) {
        Item item = itemsRepository.findById(id);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found");
        }

        if (imageFiles == null || imageFiles.isEmpty()) {
            return ResponseEntity.badRequest().body("No images provided");
        }

        try {
            for (MultipartFile imageFile : imageFiles) {
                if (imageFile == null || imageFile.isEmpty()) {
                    continue;
                }

                ItemImage image = new ItemImage(imageFile.getBytes(), imageFile.getContentType());
                item.addImage(image);
            }

            itemsRepository.save(item);
            return ResponseEntity.ok("Images uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload images");
        }
    }


    @GetMapping("/{id}/images")
    public ResponseEntity<List<Map<String, String>>> getItemImages(@PathVariable int id) {
        Item item = itemsRepository.findById(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }

        List<Map<String, String>> images = item.getImages().stream()
                .map(image -> Map.of(
                        "id", String.valueOf(image.getId()),
                        "contentType", image.getContentType() != null ? image.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE,
                        "data", Base64.getEncoder().encodeToString(image.getData())
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(images);
    }

    @GetMapping("/{itemId}/images/{imageId}")
    public ResponseEntity<byte[]> getItemImage(@PathVariable int itemId, @PathVariable Long imageId) {
        Item item = itemsRepository.findById(itemId);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }

        return itemImageRepository.findById(imageId)
                .filter(image -> image.getItem() != null && image.getItem().getId() == itemId)
                .map(image -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(
                                image.getContentType() != null ? image.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                        .body(image.getData()))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{itemId}/images/{imageId}")
    public ResponseEntity<String> deleteItemImage(@PathVariable int itemId, @PathVariable Long imageId) {
        Item item = itemsRepository.findById(itemId);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found");
        }

        return itemImageRepository.findById(imageId)
                .filter(image -> image.getItem() != null && image.getItem().getId() == itemId)
                .map(image -> {
                    item.removeImage(image);
                    itemsRepository.save(item);
                    return ResponseEntity.ok("Image deleted successfully");
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found"));
    }

    //test
    // GET seller info for an item
    @GetMapping("/{id}/seller")
    public ResponseEntity<Map<String, Object>> getItemSeller(@PathVariable int id) {
        Item item = itemsRepository.findById(id);
        if (item == null || item.getSeller() == null) {
            return ResponseEntity.notFound().build();
        }

        Seller seller = item.getSeller();
        Map<String, Object> sellerInfo = Map.of(
                "id", seller.getId(),
                "username", seller.getUsername(),
                "rating", seller.getRating() != null ? seller.getRating() : 0.0,
                "totalSales", seller.getTotalSales() != null ? seller.getTotalSales() : 0
        );

        return ResponseEntity.ok(sellerInfo);
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
        Item savedItem = itemsRepository.save(item);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

}
