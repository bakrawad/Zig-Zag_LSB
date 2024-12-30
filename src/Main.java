import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    // Hide message in image using LSB with zig-zag distribution
    public static BufferedImage hideMessage(BufferedImage image, String message) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] messageBits = toBinary(message);  // Convert message to binary
        int messageLength = messageBits.length;// store the binary messeage length

        System.out.println("Message length in bits: " + messageLength);

        int bitIndex = 0;

        // Encode message length in the first 32 bits (4 bytes)
        for (int i = 0; i < 32; i++) {
            int pixel = image.getRGB(i % width, i / width);
            int blue = (pixel & 0xFF) & ~1 | ((messageLength >> (31 - i)) & 1);
            int modifiedPixel = (pixel & 0xFFFFFF00) | blue;
            image.setRGB(i % width, i / width, modifiedPixel);
        }

        // Embed the actual message in a zig-zag pattern
        for (int diag = 32; diag < width + height - 1 && bitIndex < messageLength; diag++) {
            int startX = Math.max(0, diag - height + 1);
            int startY = Math.max(0, height - 1 - diag);
            while (startX < width && startY >= 0 && bitIndex < messageLength) {
                int pixel = image.getRGB(startX, startY);
                int blue = (pixel & 0xFF) & ~1 | messageBits[bitIndex];
                int modifiedPixel = (pixel & 0xFFFFFF00) | blue;
                image.setRGB(startX, startY, modifiedPixel);
                bitIndex++;
                startX++;
                startY--;
            }
        }

        return image;
    }

    // Retrieve the hidden message from the image
    public static String retrieveMessage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Read the message length from the first 32 bits (4 bytes)
        int messageLength = 0;
        for (int i = 0; i < 32; i++) {
            int pixel = image.getRGB(i % width, i / width);
            int blue = pixel & 1;
            messageLength = (messageLength << 1) | blue;
        }

        int[] messageBits = new int[messageLength];
        int bitIndex = 0;

        // Retrieve the message using zig-zag traversal
        for (int diag = 32; diag < width + height - 1 && bitIndex < messageLength; diag++) {
            int startX = Math.max(0, diag - height + 1);
            int startY = Math.max(0, height - 1 - diag);
            while (startX < width && startY >= 0 && bitIndex < messageLength) {
                int pixel = image.getRGB(startX, startY);
                messageBits[bitIndex] = pixel & 1;
                bitIndex++;
                startX++;
                startY--;
            }
        }

        return fromBinary(messageBits);  // Convert binary array back to text
    }

    // Convert message to binary array
    private static int[] toBinary(String message) {
        byte[] bytes = message.getBytes(); // extract the pytes
        int[] binary = new int[bytes.length * 8]; // the binary array with range of num of pytes *  8 for bits
        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < 8; j++) {
                binary[i * 8 + j] = (bytes[i] >> (7 - j)) & 1; // make sure to store the bits in the array as the representation for file
            }
        }
        return binary;
    }

    // Convert binary array to message
    private static String fromBinary(int[] binary) {
        byte[] bytes = new byte[binary.length / 8];
        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < 8; j++) {
                bytes[i] <<= 1;// Shift bytes[i] one bit to the left
                bytes[i] |= binary[i * 8 + j];
            }
        }
        return new String(bytes);
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose an option:");
        System.out.println("1 - Encode a message in an image");
        System.out.println("2 - Retrieve a message from an image");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1 -> {
                System.out.print("Enter the path of the image to encode : ");
                String imagePath = scanner.nextLine();
                BufferedImage image = ImageIO.read(new File(imagePath));

                System.out.print("Enter the secret message to hide: ");
                String message = scanner.nextLine();

                BufferedImage encodedImage = hideMessage(image, message);
                ImageIO.write(encodedImage, "png", new File("encoded_image.png"));
                System.out.println("Message encoded successfully in 'encoded_image.png'");
            }
            case 2 -> {
                System.out.print("Enter the path of the encoded image : ");
                String imagePath = scanner.nextLine();
                BufferedImage image = ImageIO.read(new File(imagePath));

                String retrievedMessage = retrieveMessage(image);
                System.out.println("Retrieved message: " + retrievedMessage);
            }
            default -> System.out.println("Invalid option. Please choose 1 or 2.");
        }
        scanner.close();
    }
}