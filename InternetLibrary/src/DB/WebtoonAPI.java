package DB;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class WebtoonAPI {
    private static final String BASE_URL = "https://korea-webtoon-api-cc7dda2f0d77.herokuapp.com/webtoons";
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=InternetLibrary;encrypt=true;trustServerCertificate=true;";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "1q2w3e4r@";

    // API 호출 메서드
    public JSONArray fetchWebtoons(String provider, int page, String updateDay) {
        try {
            String apiUrl = String.format(
                "%s?provider=%s&page=%d&perPage=9&sort=ASC&updateDay=%s",
                BASE_URL, provider, page, updateDay
            );
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse.getJSONArray("webtoons");
            } else {
                System.out.println("API 호출 실패: 응답 코드 " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONArray(); // 실패 시 빈 배열 반환
    }

    // 키워드 검색 메서드
    public JSONArray searchWebtoons(String keyword, String provider, int page) {
        try {
            String apiUrl = String.format(
                "%s?keyword=%s&provider=%s&page=%d&perPage=30&sort=ASC",
                BASE_URL, URLEncoder.encode(keyword, "UTF-8"), provider, page
            );

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse.getJSONArray("webtoons");
            } else {
                System.out.println("API 호출 실패: 응답 코드 " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONArray(); // 실패 시 빈 배열 반환
    }

    // 북마크 저장 메서드
    public void addBookmark(String id, String title, String authors, String thumbnail, String url, String provider) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT INTO Bookmarks (id, title, authors, thumbnail, url, provider) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, id);
            pstmt.setString(2, title);
            pstmt.setString(3, authors);
            pstmt.setString(4, thumbnail);
            pstmt.setString(5, url);
            pstmt.setString(6, provider);
            pstmt.executeUpdate();
            System.out.println("북마크 저장 성공: " + title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 북마크 삭제 메서드
    public void removeBookmark(String id) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "DELETE FROM Bookmarks WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, id);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("북마크 삭제 성공: " + id);
            } else {
                System.out.println("삭제할 북마크가 없습니다: " + id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 북마크 데이터 가져오기
    public JSONArray getBookmarks() {
        JSONArray bookmarks = new JSONArray();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT id, title, authors, thumbnail, url, provider FROM Bookmarks";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                JSONObject webtoon = new JSONObject();
                webtoon.put("id", rs.getString("id"));
                webtoon.put("title", rs.getString("title"));
                webtoon.put("authors", rs.getString("authors"));
                webtoon.put("thumbnail", rs.getString("thumbnail"));
                webtoon.put("url", rs.getString("url"));
                webtoon.put("provider", rs.getString("provider"));
                bookmarks.put(webtoon);
            }
            System.out.println("북마크 데이터 가져오기 성공");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookmarks;
    }
}
