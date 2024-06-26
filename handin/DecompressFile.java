import java.io.*;
import java.util.*;

public class DecompressFile {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java DecompressFile <source file> <destination file>");
            return;
        }

        String sourceFile = args[0];
        String destinationFile = args[1];

        try (FileInputStream inputStream = new FileInputStream(sourceFile);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
             FileOutputStream outputStream = new FileOutputStream(destinationFile)) {

            // Step 1: Read Huffman codes from the compressed file
            @SuppressWarnings("unchecked")
            Map<Character, String> huffmanCodes = (Map<Character, String>) objectInputStream.readObject();

            // Step 2: Reconstruct Huffman tree
            HuffmanNode root = reconstructHuffmanTree(huffmanCodes);

            // Step 3: Decode the binary data using Huffman tree
            decodeFile(inputStream, outputStream, root);

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
    }

    private static HuffmanNode reconstructHuffmanTree(Map<Character, String> huffmanCodes) {
        HuffmanNode root = new HuffmanNode('\0', 0);

        for (Map.Entry<Character, String> entry : huffmanCodes.entrySet()) {
            char[] codeArray = entry.getValue().toCharArray();

            HuffmanNode current = root;

            for (char bit : codeArray) {
                if (bit == '0') {
                    if (current.left == null) {
                        current.left = new HuffmanNode('\0', 0);
                    }
                    current = current.left;
                } else if (bit == '1') {
                    if (current.right == null) {
                        current.right = new HuffmanNode('\0', 0);
                    }
                    current = current.right;
                }
            }
            current.data = entry.getKey();
        }

        return root;
    }

    private static void decodeFile(FileInputStream inputStream, FileOutputStream outputStream, HuffmanNode root) throws IOException {
        HuffmanNode current = root;
        int bits;
        while ((bits = inputStream.read()) != -1) {
            for (int i = 7; i >= 0; i--) {
                int bit = (bits >> i) & 1;
                current = (bit == 0) ? current.left : current.right;
                if (current.left == null && current.right == null) {
                    outputStream.write(current.data);
                    current = root;
                }
            }
        }
    }
}