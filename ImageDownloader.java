import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageDownloader {

    public static void main(String[] args) {
        try {
            // 1. 读取网络上的文件并解析获取图片路径
            URL url = new URL("http://10.122.7.154/javaweb/data/images-url.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            List<String> imageUrls = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                imageUrls.add(line);
            }
            reader.close();

            // 2. 在本地C盘根目录创建一个文件夹images
            String localPath = "C:\\images";
            Path imagesDirectory = Paths.get(localPath);
            if (!Files.exists(imagesDirectory)) {
                Files.createDirectories(imagesDirectory);
            }

            AtomicInteger count = new AtomicInteger(1);
            Map<String, Long> imageSizeMap = new HashMap<>();

            // 3. 下载图片并存储到images目录
            for (String imageUrl : imageUrls) {
                URL imgUrl = new URL(imageUrl);
                String imageName = getImageNameFromURL(imageUrl);
                Path destinationPath = imagesDirectory.resolve(getLocalPathFromURL(imageUrl)).resolve(imageName);

                try (InputStream in = imgUrl.openStream()) {
                    Files.createDirectories(destinationPath.getParent());
                    Files.copy(in, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                    long imageSize = Files.size(destinationPath);
                    imageSizeMap.put(imageUrl, imageSize);
                    System.out.println("Downloaded: " + destinationPath + " Size: " + imageSize);
                } catch (IOException e) {
                    System.err.println("Failed to download: " + destinationPath);
                }
            }

            // 4. 对下载的图片按大小排序并写入到images-sorted.txt
            List<Map.Entry<String, Long>> sortedImages = new ArrayList<>(imageSizeMap.entrySet());
            sortedImages.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

            Path sortedImagePath = imagesDirectory.resolve("images-sorted.txt");
            BufferedWriter writer = Files.newBufferedWriter(sortedImagePath);
            for (Map.Entry<String, Long> entry : sortedImages) {
                writer.write(entry.getValue() + " " + entry.getKey() + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getImageNameFromURL(String imageUrl) {
        int lastSlashIndex = imageUrl.lastIndexOf('/');
        return imageUrl.substring(lastSlashIndex + 1);
    }

    private static String getLocalPathFromURL(String imageUrl) {
        String[] parts = imageUrl.split("/");
        return String.join("\\", Arrays.copyOfRange(parts, 2, parts.length - 1));
    }
}
