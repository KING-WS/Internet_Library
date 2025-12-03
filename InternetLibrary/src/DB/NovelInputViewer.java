package DB;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NovelInputViewer extends JFrame {
    private JTextField titleField;
    private JTextField authorField;
    private JTextField urlField;
    private JTextField thumbnailField;

    public NovelInputViewer() {
        setTitle("소설 입력");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 레이아웃 설정
        setLayout(new GridLayout(5, 2));

        // 제목, 작가, URL, 썸네일 입력 필드
        titleField = new JTextField();
        authorField = new JTextField();
        urlField = new JTextField();
        thumbnailField = new JTextField();

        // 저장 버튼
        JButton saveButton = new JButton("소설 저장");

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = titleField.getText();
                String author = authorField.getText();
                String url = urlField.getText();
                String thumbnail = thumbnailField.getText();

                if (title.isEmpty() || author.isEmpty() || url.isEmpty() || thumbnail.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "모든 필드를 입력해 주세요.");
                } else {
                    // DB에 소설 정보 저장
                    WebtoonAPI api = new WebtoonAPI();
                    api.addBookmark("novel_" + System.currentTimeMillis(), title, author, thumbnail, url, "NOVEL");
                    JOptionPane.showMessageDialog(null, "소설이 저장되었습니다.");
                    dispose();  // 소설 입력 화면 닫기
                }
            }
        });

        // 화면에 필드와 버튼 추가
        add(new JLabel("제목:"));
        add(titleField);
        add(new JLabel("작가:"));
        add(authorField);
        add(new JLabel("URL:"));
        add(urlField);
        add(new JLabel("썸네일 URL:"));
        add(thumbnailField);
        add(saveButton);
    }
}
