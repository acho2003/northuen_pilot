package bt.northuen.api.config;

import bt.northuen.api.entity.*;
import bt.northuen.api.repository.DriverRepository;
import bt.northuen.api.repository.PickDropOrderRepository;
import bt.northuen.api.repository.ProductRepository;
import bt.northuen.api.repository.UserRepository;
import bt.northuen.api.repository.VendorRepository;
import bt.northuen.api.repository.WalletAccountRepository;
import bt.northuen.api.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BootstrapData implements ApplicationRunner {
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    private final DriverRepository driverRepository;
    private final PickDropOrderRepository pickDropOrderRepository;
    private final WalletAccountRepository walletAccountRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL:}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    @Value("${ADMIN_FULL_NAME:Northuen Admin}")
    private String adminFullName;

    @Value("${ADMIN_PHONE:+97517000000}")
    private String adminPhone;

    @Value("${DEMO_DATA_ENABLED:true}")
    private boolean demoDataEnabled;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminEmail != null && !adminEmail.isBlank() && adminPassword != null && !adminPassword.isBlank()) {
            var email = adminEmail.toLowerCase();
            if (!userRepository.existsByEmail(email)) {
                var admin = new User();
                admin.setFullName(adminFullName);
                admin.setEmail(email);
                admin.setPhone(adminPhone);
                admin.setPasswordHash(passwordEncoder.encode(adminPassword));
                admin.setRole(Role.ADMIN);
                admin.setActive(true);
                userRepository.save(admin);
            }
        }
        if (demoDataEnabled) {
            seedDemoData();
        }
    }

    private void seedDemoData() {
        var customer = user("Chencho", "customer@northuen.bt", "+97517123456", Role.CUSTOMER);
        var restaurantOwner = user("Sonam Dorji", "sonam.kitchen@northuen.bt", "+97517110001", Role.VENDOR);
        var groceryOwner = user("Karma Store", "karma.store@northuen.bt", "+97517110002", Role.VENDOR);
        var driverOne = user("Pema Driver", "pema.driver@northuen.bt", "+97517110003", Role.DRIVER);
        var driverTwo = user("Deki Driver", "deki.driver@northuen.bt", "+97517110004", Role.DRIVER);

        var kitchen = vendor(restaurantOwner, "Dochula Kitchen", "FOOD", "Ema datshi, momos, suja, and quick Bhutanese lunches.", "Norzin Lam, Thimphu", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c");
        var grocery = vendor(groceryOwner, "Karma Everyday Store", "SHOP", "Groceries, household basics, snacks, and drinks delivered around Thimphu.", "Changzamtog, Thimphu", "https://images.unsplash.com/photo-1542838132-92c53300491e");

        product(kitchen, "Ema Datshi Set", "Cheese chili curry with red rice.", "FOOD", "180.00");
        product(kitchen, "Beef Momo", "Steamed momos with ezay.", "FOOD", "140.00");
        product(kitchen, "Suja & Khapse", "Butter tea with crisp khapse.", "FOOD", "90.00");
        product(grocery, "Local Eggs", "Tray of fresh local eggs.", "SHOP", "210.00");
        product(grocery, "Red Rice 5kg", "Bhutanese red rice pack.", "SHOP", "420.00");
        product(grocery, "Mineral Water Pack", "Six bottles for home delivery.", "SHOP", "120.00");

        driver(driverOne, "Bike", "TH-BIKE-101", true);
        driver(driverTwo, "Car", "TH-CAR-202", true);

        walletRecharge(customer, "500.00", "SEED-CUSTOMER-500", "Pilot manual token recharge");
        walletRecharge(driverOne, "120.00", "SEED-PEMA-120", "Runner pilot token float");
        walletRecharge(driverTwo, "120.00", "SEED-DEKI-120", "Runner pilot token float");
        pickDropJob(customer);
    }

    private User user(String fullName, String email, String phone, Role role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            var user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPhone(phone);
            user.setPasswordHash(passwordEncoder.encode("password123"));
            user.setRole(role);
            return userRepository.save(user);
        });
    }

    private Vendor vendor(User owner, String name, String category, String description, String address, String imageUrl) {
        return vendorRepository.findByOwner(owner).orElseGet(() -> {
            var vendor = new Vendor();
            vendor.setOwner(owner);
            vendor.setName(name);
            vendor.setCategory(category);
            vendor.setDescription(description);
            vendor.setAddress(address);
            vendor.setLatitude(new BigDecimal("27.4728000"));
            vendor.setLongitude(new BigDecimal("89.6390000"));
            vendor.setImageUrl(imageUrl);
            vendor.setOpen(true);
            return vendorRepository.save(vendor);
        });
    }

    private void product(Vendor vendor, String name, String description, String category, String price) {
        boolean exists = productRepository.findByVendor(vendor).stream().anyMatch(product -> product.getName().equalsIgnoreCase(name));
        if (exists) {
            return;
        }
        var product = new Product();
        product.setVendor(vendor);
        product.setName(name);
        product.setDescription(description);
        product.setCategory(category);
        product.setPrice(new BigDecimal(price));
        product.setAvailable(true);
        productRepository.save(product);
    }

    private void driver(User user, String vehicleType, String licenseNumber, boolean available) {
        driverRepository.findByUser(user).orElseGet(() -> {
            var driver = new Driver();
            driver.setUser(user);
            driver.setVehicleType(vehicleType);
            driver.setLicenseNumber(licenseNumber);
            driver.setAvailable(available);
            driver.setCurrentLatitude(new BigDecimal("27.4782000"));
            driver.setCurrentLongitude(new BigDecimal("89.6320000"));
            return driverRepository.save(driver);
        });
    }

    private void walletRecharge(User user, String amount, String reference, String note) {
        if (walletTransactionRepository.existsByReference(reference)) {
            return;
        }
        var rechargeAmount = new BigDecimal(amount);
        var wallet = walletAccountRepository.findByUser(user).orElseGet(() -> {
            var account = new WalletAccount();
            account.setUser(user);
            account.setTokenBalance(BigDecimal.ZERO);
            return walletAccountRepository.save(account);
        });
        wallet.setTokenBalance(wallet.getTokenBalance().add(rechargeAmount));
        walletAccountRepository.save(wallet);

        var transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setType(WalletTransactionType.MANUAL_RECHARGE);
        transaction.setAmount(rechargeAmount);
        transaction.setBalanceAfter(wallet.getTokenBalance());
        transaction.setReference(reference);
        transaction.setNote(note);
        walletTransactionRepository.save(transaction);
    }

    private void pickDropJob(User customer) {
        var openJobs = pickDropOrderRepository.findByDriverIsNullAndStatusInOrderByCreatedAtDesc(List.of(PickDropStatus.PENDING));
        boolean demoExists = openJobs.stream().anyMatch(order -> order.getItemDescription().contains("Seeded pilot job"));
        if (demoExists) {
            return;
        }
        var order = new PickDropOrder();
        order.setCustomer(customer);
        order.setPickupAddress("Clock Tower Square, Thimphu");
        order.setPickupLat(new BigDecimal("27.4728000"));
        order.setPickupLng(new BigDecimal("89.6390000"));
        order.setDropAddress("Motithang, Thimphu");
        order.setDropLat(new BigDecimal("27.4850000"));
        order.setDropLng(new BigDecimal("89.6250000"));
        order.setItemType("Documents");
        order.setItemDescription("Seeded pilot job - envelope delivery for driver testing");
        order.setEstimatedDistanceKm(new BigDecimal("1.94"));
        order.setEstimatedPrice(new BigDecimal("88.80"));
        order.setStatus(PickDropStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        pickDropOrderRepository.save(order);
    }
}
