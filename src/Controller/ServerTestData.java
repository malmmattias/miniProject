package Controller;

import Model.ItemCondition;
import Model.Product;
import Model.Status;

public class ServerTestData {

    public static void addData(ResizableProductsArray<Product> products) {
        Product product1 = new Product.Builder("iphone", 1000, 2022, "john", "none")
                .color("Black")
                .itemCondidtion(ItemCondition.NEW)
                .status(Status.AVAILABLE)
                .build();

        Product product2 = new Product.Builder("mac", 2000, 2021, "john", "none")
                .color("Silver")
                .itemCondidtion(ItemCondition.USED)
                .status(Status.AVAILABLE)
                .build();

        products.add(product1);
        products.add(product2);
        //System.out.println("Products in the array:");
        for (int i = 0; i < products.size(); i++) {
            //System.out.println(products.get(i));
        }
    }
}
