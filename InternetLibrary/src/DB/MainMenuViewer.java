package DB;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainMenuViewer extends JFrame {
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=InternetLibrary;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "1q2w3e4r@";

    public MainMenuViewer() {
        setTitle("메인 메뉴");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

     // 상단에 배너 이미지 추가
        JLabel bannerLabel = new JLabel();
        bannerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            // resources 폴더에 있는 이미지 파일을 클래스패스를 통해 로드
            URL bannerUrl = getClass().getResource("/banner.jpg");
            if (bannerUrl != null) {
                Image bannerImage = ImageIO.read(bannerUrl).getScaledInstance(750, 500, Image.SCALE_SMOOTH);
                bannerLabel.setIcon(new ImageIcon(bannerImage));
            } else {
                bannerLabel.setText("이미지를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            bannerLabel.setText("배너 이미지 없음");
            e.printStackTrace();
        }
        add(bannerLabel, BorderLayout.NORTH);

        // 버튼 패널 생성
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        
        JButton webtoonButton = new JButton("웹툰 보기");
        JButton novelButton = new JButton("소설 관리");
        
        buttonPanel.add(webtoonButton);
        buttonPanel.add(novelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        webtoonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WebtoonTabbedViewer webtoonViewer = new WebtoonTabbedViewer();
                webtoonViewer.setVisible(true);
                dispose();
            }
        });

        novelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setContentPane(createNovelPanel());
                revalidate();
                repaint();
            }
        });
    }

    private JPanel createNovelPanel() {
        JPanel novelPanel = new JPanel(new BorderLayout());

        DefaultListModel<Novel> listModel = new DefaultListModel<>();
        JList<Novel> novelList = new JList<>(listModel);
        novelList.setCellRenderer(new NovelCellRenderer());
        novelList.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(novelList);
        novelPanel.add(scrollPane, BorderLayout.CENTER);

        loadNovels(listModel);

        // 우측 버튼 패널 생성
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton addButton = new JButton("등록");
        addButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        addButton.setBackground(new Color(0, 123, 255));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> openAddNovelDialog(listModel));
        buttonPanel.add(addButton);

        JButton updateButton = new JButton("수정");
        updateButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        updateButton.setBackground(new Color(255, 193, 7));
        updateButton.setForeground(Color.WHITE);
        updateButton.setFocusPainted(false);
        updateButton.addActionListener(e -> openUpdateNovelDialog(novelList, listModel));
        buttonPanel.add(updateButton);

        JButton deleteButton = new JButton("삭제");
        deleteButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.addActionListener(e -> deleteSelectedNovel(novelList, listModel));
        buttonPanel.add(deleteButton);

        novelPanel.add(buttonPanel, BorderLayout.EAST);

        novelList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Novel selectedNovel = novelList.getSelectedValue();
                    if (selectedNovel != null) {
                        try {
                            String url = selectedNovel.getUrl();
                            // URL 형식 보완
                            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                url = "http://" + url; // 기본적으로 http를 추가
                            }
                            Desktop.getDesktop().browse(new URI(url));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(novelList, "URL을 열 수 없습니다: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });


        return novelPanel;
    }

    private void loadNovels(DefaultListModel<Novel> listModel) {
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement("SELECT title, author, url FROM novel")) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String title = rs.getString("title");
                String author = rs.getString("author");
                String url = rs.getString("url");
                listModel.addElement(new Novel(title, author, url));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openAddNovelDialog(DefaultListModel<Novel> listModel) {
    Connection conn = connect(); // 데이터베이스 연결
    if (conn != null) {
        AddNovelDialog dialog = new AddNovelDialog(this, listModel, conn);
        dialog.setVisible(true); // 팝업 창 표시
    } else {
        JOptionPane.showMessageDialog(this, "데이터베이스 연결 실패!", "오류", JOptionPane.ERROR_MESSAGE);
    }
}

private void openUpdateNovelDialog(JList<Novel> novelList, DefaultListModel<Novel> listModel) {
    Novel selectedNovel = novelList.getSelectedValue();
    if (selectedNovel != null) {
        Connection conn = connect(); // 데이터베이스 연결
        if (conn != null) {
            AddNovelDialog dialog = new AddNovelDialog(this, listModel, conn, selectedNovel);
            dialog.setVisible(true); // 수정 다이얼로그 표시
        } else {
            JOptionPane.showMessageDialog(this, "데이터베이스 연결 실패!", "오류", JOptionPane.ERROR_MESSAGE);
        }
    } else {
        JOptionPane.showMessageDialog(this, "수정할 소설을 선택해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
    }
}

    private void deleteSelectedNovel(JList<Novel> novelList, DefaultListModel<Novel> listModel) {
        Novel selectedNovel = novelList.getSelectedValue();

        if (selectedNovel != null) {
            listModel.removeElement(selectedNovel);
            try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM novel WHERE title = ? AND author = ? AND url = ?")) {
                pstmt.setString(1, selectedNovel.getTitle());
                pstmt.setString(2, selectedNovel.getAuthor());
                pstmt.setString(3, selectedNovel.getUrl());
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "소설이 성공적으로 삭제되었습니다!");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "삭제할 소설을 선택해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Connection connect() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            System.out.println("데이터베이스에 연결되었습니다!");
            return conn;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainMenuViewer mainMenu = new MainMenuViewer();
                mainMenu.setVisible(true);
            }
        });
    }
}

class Novel {
    private String title;
    private String author;
    private String url;

    public Novel(String title, String author, String url) {
        this.title = title;
        this.author = author;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return title + " 작가: " + author;
    }
}

class NovelCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (c instanceof JLabel && value instanceof Novel) {
            Novel novel = (Novel) value;
            JLabel label = (JLabel) c;
            label.setText(novel.getTitle() + " 작가: " + novel.getAuthor());
        }
        return c;
    }
}

class AddNovelDialog extends JDialog {
    public AddNovelDialog(JFrame parent, DefaultListModel<Novel> listModel, Connection conn) {
        this(parent, listModel, conn, null);
    }

    public AddNovelDialog(JFrame parent, DefaultListModel<Novel> listModel, Connection conn, Novel existingNovel) {
        super(parent, existingNovel == null ? "소설 추가" : "소설 수정", true);

        if (conn == null) {
            JOptionPane.showMessageDialog(parent, "데이터베이스 연결이 필요합니다.", "오류", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 필드 생성
        JTextField titleField = new JTextField(20);
        JTextField authorField = new JTextField(20);
        JTextField urlField = new JTextField(20);

        if (existingNovel != null) {
            titleField.setText(existingNovel.getTitle());
            authorField.setText(existingNovel.getAuthor());
            urlField.setText(existingNovel.getUrl());
        }

        // UI 배치
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("제목:"), gbc);
        gbc.gridx = 1;
        add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("작가:"), gbc);
        gbc.gridx = 1;
        add(authorField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("URL:"), gbc);
        gbc.gridx = 1;
        add(urlField, gbc);

        // 저장 버튼
        JButton saveButton = new JButton(existingNovel == null ? "추가" : "수정");
        saveButton.setBackground(new Color(40, 167, 69));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        saveButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String url = urlField.getText().trim();

            if (title.isEmpty() || author.isEmpty() || url.isEmpty()) {
                JOptionPane.showMessageDialog(this, "모든 필드를 채워주세요.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    existingNovel == null 
                        ? "INSERT INTO novel (title, author, url) VALUES (?, ?, ?)" 
                        : "UPDATE novel SET title = ?, author = ?, url = ? WHERE title = ? AND author = ? AND url = ?")) {
                pstmt.setString(1, title);
                pstmt.setString(2, author);
                pstmt.setString(3, url);

                if (existingNovel != null) {
                    pstmt.setString(4, existingNovel.getTitle());
                    pstmt.setString(5, existingNovel.getAuthor());
                    pstmt.setString(6, existingNovel.getUrl());
                }

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    if (existingNovel != null) {
                        listModel.removeElement(existingNovel);
                    }
                    listModel.addElement(new Novel(title, author, url));
                    JOptionPane.showMessageDialog(this, existingNovel == null 
                            ? "소설이 추가되었습니다!" 
                            : "소설이 수정되었습니다!");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "작업에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "데이터베이스 작업 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 1;
        gbc.gridy = 3;
        add(saveButton, gbc);

        pack();
        setLocationRelativeTo(parent);
    }
}

           
