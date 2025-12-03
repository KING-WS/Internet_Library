package DB;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WebtoonTabbedViewer extends JFrame {
    private final WebtoonAPI api = new WebtoonAPI();
    private final Map<String, Integer> naverPageMap = new HashMap<>();
    private final Map<String, Integer> kakaoPageMap = new HashMap<>();
    private JPanel bookmarkPanel;
    private JPanel searchResultPanel;

    public WebtoonTabbedViewer() {
        setTitle("Internet Library");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // 네이버와 카카오 탭 추가
        JPanel naverTab = createProviderTab("NAVER", naverPageMap);
        JPanel kakaoTab = createProviderTab("KAKAO", kakaoPageMap);
        tabbedPane.addTab("네이버", naverTab);
        tabbedPane.addTab("카카오", kakaoTab);

        // 북마크 탭 추가
        bookmarkPanel = createBookmarkPanel();
        tabbedPane.addTab("북마크", new JScrollPane(bookmarkPanel));

        // 검색 탭 추가
        JPanel searchTab = createSearchTab();
        tabbedPane.addTab("검색", searchTab);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createSearchTab() {
        JPanel searchTab = new JPanel(new BorderLayout());

        // 검색 입력 필드 및 버튼
        JPanel searchPanel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField();
        JButton searchButton = new JButton("검색");

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        // 검색 결과 표시 패널
        searchResultPanel = new JPanel();
        searchResultPanel.setLayout(new GridLayout(0, 3, 10, 10));
        JScrollPane scrollPane = new JScrollPane(searchResultPanel);

        // 검색 버튼 동작
        searchButton.addActionListener(e -> {
            String query = searchField.getText().trim();
            if (!query.isEmpty()) {
                searchWebtoons(query);
            }
        });

        searchTab.add(searchPanel, BorderLayout.NORTH);
        searchTab.add(scrollPane, BorderLayout.CENTER);

        return searchTab;
    }

    private void searchWebtoons(String keyword) {
        searchResultPanel.removeAll();

        SwingWorker<JSONArray, Void> worker = new SwingWorker<>() {
            @Override
            protected JSONArray doInBackground() throws Exception {
                // 검색어로 네이버와 카카오에서 데이터를 가져옴
                JSONArray naverResults = api.searchWebtoons(keyword, "NAVER", 1);
                JSONArray kakaoResults = api.searchWebtoons(keyword, "KAKAO", 1);

                // 결과를 병합
                JSONArray combinedResults = new JSONArray();
                for (int i = 0; i < naverResults.length(); i++) {
                    combinedResults.put(naverResults.getJSONObject(i));
                }
                for (int i = 0; i < kakaoResults.length(); i++) {
                    combinedResults.put(kakaoResults.getJSONObject(i));
                }
                return combinedResults;
            }

            @Override
            protected void done() {
                try {
                    JSONArray results = get();
                    updateUI("SEARCH", searchResultPanel, results);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    private void loadWebtoonsAsync(String provider, String day, JPanel webtoonPanel, Map<String, Integer> pageMap) {
        SwingWorker<JSONArray, Void> worker = new SwingWorker<>() {
            @Override
            protected JSONArray doInBackground() throws Exception {
                Integer currentPage = pageMap.get(day);
                if (currentPage == null) {
                    currentPage = 1;
                    pageMap.put(day, currentPage);
                }
                return api.fetchWebtoons(provider, currentPage, day);
            }

            @Override
            protected void done() {
                try {
                    JSONArray webtoons = get();
                    updateUI(provider, webtoonPanel, webtoons);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }


    private JPanel createProviderTab(String provider, Map<String, Integer> pageMap) {
        JPanel providerTab = new JPanel(new BorderLayout());
        JTabbedPane dayTabbedPane = new JTabbedPane();

        String[] days = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
        Map<String, JPanel> panelMap = new HashMap<>();

        for (String day : days) {
            JPanel dayPanel = createDayPanel(provider, day, pageMap);
            dayTabbedPane.addTab(getDayLabel(day), dayPanel);
            panelMap.put(day, dayPanel);
            pageMap.put(day, 1); // Initialize page map
        }

        dayTabbedPane.addChangeListener(e -> {
            int selectedIndex = dayTabbedPane.getSelectedIndex();
            if (selectedIndex != -1) {
                String selectedDay = days[selectedIndex];
                JPanel dayPanel = panelMap.get(selectedDay);
                JScrollPane scrollPane = (JScrollPane) dayPanel.getComponent(0);
                JPanel webtoonPanel = (JPanel) scrollPane.getViewport().getView();

                pageMap.put(selectedDay, 1);
                loadWebtoonsAsync(provider, selectedDay, webtoonPanel, pageMap);
            }
        });

        providerTab.add(dayTabbedPane, BorderLayout.CENTER);
        return providerTab;
    }

    private JPanel createDayPanel(String provider, String day, Map<String, Integer> pageMap) {
        JPanel dayPanel = new JPanel();
        dayPanel.setLayout(new BorderLayout());

        JPanel webtoonPanel = new JPanel();
        webtoonPanel.setLayout(new GridLayout(0, 3, 10, 10));
        JScrollPane scrollPane = new JScrollPane(webtoonPanel);

        JPanel buttonPanel = new JPanel();
        JButton previousPageButton = new JButton("이전 페이지");
        JButton nextPageButton = new JButton("다음 페이지");

        previousPageButton.addActionListener(e -> loadPreviousPage(provider, day, webtoonPanel, pageMap));
        nextPageButton.addActionListener(e -> loadNextPage(provider, day, webtoonPanel, pageMap));

        buttonPanel.add(previousPageButton);
        buttonPanel.add(nextPageButton);

        dayPanel.add(scrollPane, BorderLayout.CENTER);
        dayPanel.add(buttonPanel, BorderLayout.SOUTH);

        loadWebtoonsAsync(provider, day, webtoonPanel, pageMap);

        return dayPanel;
    }

    private JPanel createBookmarkPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 3, 10, 10));
        loadBookmarks(panel);
        return panel;
    }

    private void loadBookmarks(JPanel bookmarkPanel) {
        bookmarkPanel.removeAll();

        SwingWorker<JSONArray, Void> worker = new SwingWorker<>() {
            @Override
            protected JSONArray doInBackground() throws Exception {
                return api.getBookmarks(); // 북마크 데이터를 API에서 가져옴
            }

            @Override
            protected void done() {
                try {
                    JSONArray bookmarks = get();
                    for (int i = 0; i < bookmarks.length(); i++) {
                        JSONObject webtoon = bookmarks.getJSONObject(i);

                        String id = webtoon.getString("id");
                        String title = webtoon.getString("title");
                        String authors = webtoon.getString("authors");
                        String thumbnail = webtoon.getString("thumbnail");
                        String url = webtoon.getString("url");
                        String provider = webtoon.getString("provider");

                        JPanel webtoonCard = createWebtoonCard(id, title, authors, new JSONArray().put(thumbnail), url, provider, true);
                        bookmarkPanel.add(webtoonCard);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                bookmarkPanel.revalidate();
                bookmarkPanel.repaint();
            }
        };
        worker.execute();
    }

    private void updateUI(String provider, JPanel webtoonPanel, JSONArray webtoons) {
        webtoonPanel.removeAll();

        JSONArray bookmarks = api.getBookmarks();
        Set<String> bookmarkedIds = new HashSet<>();
        for (int i = 0; i < bookmarks.length(); i++) {
            bookmarkedIds.add(bookmarks.getJSONObject(i).getString("id"));
        }

        for (int i = 0; i < webtoons.length(); i++) {
            JSONObject webtoon = webtoons.getJSONObject(i);

            String id = webtoon.getString("id");
            String title = webtoon.getString("title");
            JSONArray authorsArray = webtoon.getJSONArray("authors");
            JSONArray thumbnailArray = webtoon.getJSONArray("thumbnail");
            String url = webtoon.getString("url");

            String authors = authorsArray.length() > 0 ? authorsArray.getString(0) : "Unknown";

            boolean isBookmarked = bookmarkedIds.contains(id);

            JPanel webtoonCard = createWebtoonCard(id, title, authors, thumbnailArray, url, provider, isBookmarked);
            webtoonPanel.add(webtoonCard);
        }

        webtoonPanel.revalidate();
        webtoonPanel.repaint();
    }

    private JPanel createWebtoonCard(String id, String title, String authors, JSONArray thumbnails, String webtoonUrl, String provider, boolean isBookmarked) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JLabel imageLabel = new JLabel("이미지 없음", SwingConstants.CENTER);
        card.add(imageLabel, BorderLayout.CENTER);

        if (thumbnails.length() > 0) {
            String imageUrl = thumbnails.getString(0);
            SwingWorker<Image, Void> imageWorker = new SwingWorker<>() {
                @Override
                protected Image doInBackground() throws Exception {
                    URL url = new URL(imageUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                    InputStream in = conn.getInputStream();
                    return ImageIO.read(in).getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                }

                @Override
                protected void done() {
                    try {
                        Image image = get();
                        imageLabel.setIcon(new ImageIcon(image));
                        imageLabel.setText("");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            imageWorker.execute();
        }

        JButton actionButton = new JButton(isBookmarked ? "북마크 삭제" : "북마크 추가");
        actionButton.addActionListener(e -> {
            if (!isBookmarked) {
                api.addBookmark(id, title, authors, thumbnails.length() > 0 ? thumbnails.getString(0) : "", webtoonUrl, provider);
                actionButton.setText("북마크 추가됨");
                actionButton.setEnabled(false);
                loadBookmarks(bookmarkPanel);
            } else {
                api.removeBookmark(id);
                loadBookmarks(bookmarkPanel);
            }
        });

        card.add(actionButton, BorderLayout.SOUTH);

        JLabel titleLabel = new JLabel("<html><b>" + title + "</b></html>", SwingConstants.CENTER);
        JLabel authorsLabel = new JLabel("작가: " + authors, SwingConstants.CENTER);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(titleLabel);
        textPanel.add(authorsLabel);

        card.add(textPanel, BorderLayout.NORTH);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.browse(new URI(webtoonUrl));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        return card;
    }

    private void loadNextPage(String provider, String day, JPanel webtoonPanel, Map<String, Integer> pageMap) {
        int currentPage = pageMap.get(day);
        pageMap.put(day, currentPage + 1);
        loadWebtoonsAsync(provider, day, webtoonPanel, pageMap);
    }

    private void loadPreviousPage(String provider, String day, JPanel webtoonPanel, Map<String, Integer> pageMap) {
        int currentPage = pageMap.get(day);
        if (currentPage > 1) {
            pageMap.put(day, currentPage - 1);
            loadWebtoonsAsync(provider, day, webtoonPanel, pageMap);
        } else {
            System.out.println("이전 페이지가 없습니다.");
        }
    }

    private String getDayLabel(String day) {
        return switch (day) {
            case "MON" -> "월요일";
            case "TUE" -> "화요일";
            case "WED" -> "수요일";
            case "THU" -> "목요일";
            case "FRI" -> "금요일";
            case "SAT" -> "토요일";
            case "SUN" -> "일요일";
            default -> "";
        };
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WebtoonTabbedViewer viewer = new WebtoonTabbedViewer();
            viewer.setVisible(true);
        });
    }
}
