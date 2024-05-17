package com.example.stock_price.GUI;

import com.example.stock_price.Crawler.Stock_crawler;
import com.example.stock_price.DB.Stock_db;
import com.example.stock_price.Model.Stock;
import com.example.stock_price.Model.Stock_State;
import com.example.stock_price.Test.StockAPI;

import java.awt.*;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;

public class Template_frame extends JFrame {
    private JLabel title;
    private JTextField textField;
    private DefaultComboBoxModel model = new DefaultComboBoxModel();
    private JButton addButton;
    private JScrollPane dashboard_scrollPane;
    private JScrollPane list_scrollPane;
    private JPanel dashBoard_container;
    private JPanel scrollPane_container;
    private JPanel textField_and_button_container;
    private DefaultListModel<Stock> list_model = new DefaultListModel<Stock>();
    private JList<Stock> buy_stock_list;
    private Font font = new Font("新細明體",Font.BOLD,25);
    private JPanel list_and_scrollPanel_container;
    public ArrayList<Stock> interest_stock = new ArrayList<Stock>();
    private ArrayList<String> stock_id = new ArrayList<String>();
    private JComboBox stock_combobox;
    private Stock_crawler crawler = new Stock_crawler();
    private Stock_db db ;
    private final Object lock = new Object();
    private List<Map<String,Object>> stock_api_response;
    private StockAPI stockAPI;
    private ArrayList<String> stock_id_list = new ArrayList<>();

    private int card_height = 200;
    private int card_width = 330;


    public Template_frame(){
        super("stock_price_app");
        setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS) );

        db = new Stock_db("Stock_db");
        stock_id = crawler.getStock_id();
        interest_stock.addAll(db.get_all_stock());
        for (Stock s: interest_stock){
            stock_id_list.add(s.getId());
        }

        title = new JLabel("台灣股票即時資訊");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(font);
        add(title);

        textField_and_button_container = new JPanel();
        textField_and_button_container.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        textField = new JTextField();
        textField.setColumns(20);
        textField.setFont(font);
        textField.setLayout(new BorderLayout());
        stock_combobox = new JComboBox(model){
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 0);
            }
        };
        stock_combobox.setFont(font);
        textField.add(stock_combobox,BorderLayout.SOUTH);
        set_textField_doc(textField);
        set_textField_keyListener(textField);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipady=10;
        c.gridx=0;
        c.gridy=0;
        c.gridwidth=3;
        textField_and_button_container.add(textField,c);
        
        addButton = new JButton("新增");
        addButton.setPreferredSize(new Dimension(100,30));
        addButton.setFont(font);
        addButton.addActionListener(new ButtonHandler());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipady=5;
        c.gridx=3;
        c.gridy=0;
        c.gridwidth=1;
        textField_and_button_container.add(addButton,c);
        addToPane(textField_and_button_container);

        list_and_scrollPanel_container = new JPanel(new GridBagLayout());

        dashBoard_container = new JPanel();
        dashBoard_container.setLayout(new GridBagLayout());
        paint_card_in_db();


        dashboard_scrollPane = new JScrollPane(dashBoard_container);
        dashboard_scrollPane.setPreferredSize(new Dimension(card_width*3+10,card_height*3+10));

        scrollPane_container = new JPanel(new FlowLayout(FlowLayout.CENTER));
        scrollPane_container.add(dashboard_scrollPane);

        add(scrollPane_container);
        stockAPI = new StockAPI();
        stockAPI.add_stock_to_api(stock_id_list);
        fetch_data_with_api();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(card_width*3+100,900);
        setVisible(true);
    }

    private static void setAdjusting(JComboBox cbInput, boolean adjusting) {
        cbInput.putClientProperty("is_adjusting", adjusting);
    }
    private void addToPane(Component comp){
        JPanel pane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pane.setPreferredSize(new Dimension(900,26));
        pane.add(comp);
        add(pane);
    }

    private void paint_card_in_db(){
        for (Stock stock : interest_stock){
            int index = interest_stock.indexOf(stock);
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = index % 3;
            c.gridy = index / 3;
            c.gridwidth = 1;
            dashBoard_container.add(new Stock_card(stock), c);
        }
    }

    private void fetch_data_with_api(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat s_sdf = new SimpleDateFormat("ss");
                try {
                    while(true){
                        while(Integer.valueOf(s_sdf.format(new Date()))%5 != 3) ;
                        Thread.sleep(1000);
                        if (interest_stock.size() != 0){
                            stock_api_response = stockAPI.get_response();
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
        thread.start();
    }

    private void add_to_dashBoard(Stock stock){
        synchronized (lock) {
                interest_stock.add(stock);
                db.insert_stock(stock);
                System.out.println(interest_stock.toString());
                int index = interest_stock.indexOf(stock);
                System.out.println(index);
                GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.gridx = index % 3;
                c.gridy = index / 3;
                c.gridwidth = 1;
                dashBoard_container.add(new Stock_card(stock), c);
                dashBoard_container.revalidate();
                dashBoard_container.repaint();
                dashboard_scrollPane.repaint();
                scrollPane_container.repaint();
        }
    }

    private boolean stock_Already_In(String id){
        for (Stock item: interest_stock){
            if (item.getId().equals(id)){
                System.out.println(item.getId());
                return true;
            }
        }
        return false;
    }

    private void set_textField_keyListener(JTextField textField){
        textField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                setAdjusting(stock_combobox, true);
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (stock_combobox.isPopupVisible()) {
                        e.setKeyCode(KeyEvent.VK_ENTER);
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    e.setSource(stock_combobox);
                    stock_combobox.dispatchEvent(e);
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        textField.setText(stock_combobox.getSelectedItem().toString());
                        stock_combobox.setPopupVisible(false);
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    stock_combobox.setPopupVisible(false);
                }
                setAdjusting(stock_combobox, false);
            }
        });
    }

    private void set_textField_doc(JTextField textField){
        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateList();
            }

            public void removeUpdate(DocumentEvent e) {
                updateList();
            }

            public void changedUpdate(DocumentEvent e) {
                updateList();
            }

            private void updateList() {
                setAdjusting(stock_combobox, true);
                model.removeAllElements();
                String input = textField.getText();
                if (!input.isEmpty()) {
                    for (String item : stock_id) {
                        if (item.toLowerCase().contains(input.toLowerCase())) {
                            model.addElement(item);
                        }
                    }
                }
                stock_combobox.setPopupVisible(model.getSize() > 0);
                setAdjusting(stock_combobox, false);
            }
        });
    }
    public void repaint_dashboard(){
//        for (Stock s:interest_stock){
//            add_to_dashBoard(s);
//        }
        synchronized (lock) {
            ArrayList<Stock> copyOfInterestStock = new ArrayList<>(interest_stock);

            for (Stock s : copyOfInterestStock) {
                int index = interest_stock.indexOf(s);
                System.out.println(index);
                GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.gridx = index % 3;
                c.gridy = index / 3;
                c.gridwidth = 1;
                dashBoard_container.add(new Stock_card(s), c);
            }
            dashBoard_container.revalidate();
            dashBoard_container.repaint();
            dashboard_scrollPane.repaint();
            scrollPane_container.repaint();
        }
    }

    private class ButtonHandler implements ActionListener {

        public void actionPerformed(ActionEvent event) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    JButton button = (JButton) event.getSource();
                    if (button == addButton) {
                        String stock_name = textField.getText();
                        for (String item : stock_id){
                            if (item.equals(stock_name)){
                                String[] split_item = item.split(" ");
                                String id = split_item[0];
                                stock_id_list.add(id);
                                if (!stock_Already_In(id))
                                {
                                    JLabel label = new JLabel("抓取資料中...");
                                    ArrayList<String> al = new ArrayList<String>();
                                    al.add(id);
                                    stockAPI.add_stock_to_api(al);
                                    stock_api_response = stockAPI.get_response();
                                    label.setFont(new Font("SansSerif", Font.BOLD, 20));
                                    GridBagConstraints c = new GridBagConstraints();
                                    c.fill = GridBagConstraints.HORIZONTAL;
                                    c.ipady = 5;
                                    c.gridx = 4;
                                    c.gridy = 0;
                                    c.gridwidth = 1;
                                    textField_and_button_container.add(label, c);
                                    textField_and_button_container.revalidate();
                                    textField_and_button_container.repaint();
                                    System.out.println(id);
                                    String name = split_item[1];
                                    String price = null;
                                    for (Map<String,Object> m :stock_api_response){
                                        if (m.get("股票代號").equals(id)){
                                            price = m.get("成交價").toString();
                                        }
                                    }
                                    System.out.println(name);
                                    if(price!= null ){
                                        if (price.equals("-")) price = null;
                                        else price = String.format("%.2f",Float.valueOf(price));
                                    }
                                    Stock stock = new Stock(name, id, price, new Date(), Stock_State.FLAT);
                                    add_to_dashBoard(stock);
                                    textField_and_button_container.remove(label);
                                    textField_and_button_container.revalidate();
                                    textField_and_button_container.repaint();
                                }else{
                                    try {
                                        JLabel label = new JLabel("已加入到儀表板");
                                        label.setFont(new Font("SansSerif", Font.BOLD, 20));
                                        GridBagConstraints c = new GridBagConstraints();
                                        c.fill = GridBagConstraints.HORIZONTAL;
                                        c.ipady = 5;
                                        c.gridx = 4;
                                        c.gridy = 0;
                                        c.gridwidth = 1;
                                        textField_and_button_container.add(label, c);
                                        textField_and_button_container.revalidate();
                                        textField_and_button_container.repaint();
                                        Thread.sleep(1000);
                                        textField_and_button_container.remove(label);
                                        textField_and_button_container.revalidate();
                                        textField_and_button_container.repaint();
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }

                                }
                            }
                        }
                    }
                }
            });
            thread.start();
        }
    }
    private class Stock_card extends JPanel {

        private Stock stock;
        //    private JLabel stock_name;
//    private JLabel stock_price;
        private ImageIcon UP_ARROW;
        private int card_height = 200;
        private int card_width = 330;
        private ImageIcon DOWN_ARROW;
        private Refresh_stock_price_task task;
        private Color c ;
        private Thread thread;
        private JButton buy_button;

        class Refresh_stock_price_task implements Runnable{
            private volatile boolean exit = false;
            private SimpleDateFormat s_sdf = new SimpleDateFormat("ss");
            private SimpleDateFormat d_sdf = new SimpleDateFormat("dd");
            public void stop() {
                exit = true;
            }

            public  Float round(Float d, int decimalPlace) {
                BigDecimal bd = new BigDecimal(Float.toString(d));
                bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
                return bd.floatValue();
            }
            @Override
            public void run() {
                try {
                    while(!exit){
                        while(Integer.valueOf(s_sdf.format(new Date()))%5 != 4 ) ;
                        Thread.sleep(1000);
//                        Random rand = new Random();
//                        float randomFloat = rand.nextFloat() * 0.4f - 0.2f;
//                        System.out.println(randomFloat);
//                        Float adjust = round(randomFloat,2);
                        String price = null;
                        String yesterday_price = null;
                        if (stock_api_response!=null){
                            for (Map<String,Object> m :stock_api_response){
                                if (m.get("股票代號").equals(stock.getId())){
                                    price = m.get("成交價").toString();
                                    yesterday_price = m.get("昨收價").toString();
                                }
                            }
                        }
                        float adjust;
                        Date now_date = resetTimeToMidnight(new Date());
                        Date last_update_date = resetTimeToMidnight(stock.getDate());

                        if ((last_update_date.before(now_date) || stock.getPrice()==null) && yesterday_price!=null ) {
                            stock.setPrice(String.format("%.2f",Float.valueOf(yesterday_price)));
                            stock.setDate(new Date());
                            db.update_price(stock.getId(),stock);
                            repaint();
                        }
                        if (price != null && !price.equals("-")){
                            if (stock.getPrice() == null)stock.setPrice(price);
                            adjust =Float.valueOf(price)-Float.valueOf(yesterday_price);
                            stock.setPrice( String.format("%.2f",Float.valueOf(price)));
                            stock.setDate(new Date());
                            db.update_price(stock.getId(),stock);
                            if (adjust>0) stock.setState(Stock_State.UP);
                            else if (adjust<0) stock.setState(Stock_State.DOWN);
                            else stock.setState(Stock_State.FLAT);
                            repaint();
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            private static Date resetTimeToMidnight(Date date) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar.getTime();
            }
        }
        public Stock_card(Stock stock){
            setLayout(new BorderLayout() );

            this.stock = stock;

            try{
                UP_ARROW =  new ImageIcon(new URL("file:image//up_arrow.png"));
                DOWN_ARROW =  new ImageIcon(new URL("file:image//down_arrow.png"));
            }catch (MalformedURLException e){
                e.printStackTrace();
            }
            task =new Refresh_stock_price_task();
            thread = new Thread(task);
            thread.start();
            buy_button = new JButton("移除");
            buy_button.addActionListener(new ButtonHandler());
            buy_button.setFont(new Font("SansSerif", Font.BOLD, 30));
            buy_button.setPreferredSize(new Dimension(120,40));
            addToPane(buy_button);
        }

        public void stop_thread(){
            task.stop();
        }
        private class Adjust_pane extends JPanel{

            @Override
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.setColor(Color.WHITE);
                g.fillRect(1, 0, card_width-1, card_height);
                g.setColor(Color.BLACK);
                g.drawLine(0, 0, 0, card_height);
                g.drawLine(card_width-1, 0, card_width-1, card_height);
                g.drawLine(0, 50-1, card_width-1, 50-1);
            }
        }

        private void addToPane(Component comp){
            Adjust_pane pane = new Adjust_pane();
            pane.setPreferredSize(new Dimension(120,50));
            pane.getPreferredSize();
            pane.add(comp);
            add(pane, BorderLayout.AFTER_LAST_LINE);
        }

        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);

            g.setColor(Color.WHITE);

            g.fillRect(0, 0, card_width, card_height);
            g.setColor(Color.BLACK);
            g.drawRect(0, 0, card_width-1, card_height-1);

//        g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 30));
            ImageIcon icon = UP_ARROW;
            g.drawImage(icon.getImage(), 30, 60, 100, 100, null);
            g.drawString(stock.getId(), 10, 40);
            g.drawString(stock.getName(), 10, 70);
            g.setFont(new Font("Time New Romans", Font.PLAIN, 28));
            g.drawString("股價:",10,100);
            switch (stock.getState()){
                case UP -> c = Color.RED;
                case DOWN -> c = Color.green;
                case FLAT -> c = Color.black;
            }
            g.setColor(c);
            g.drawString(filter_price(), 75, 100);
            g.setColor(Color.BLACK);
            SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
            g.drawString("時間:"+sdf.format(stock.getDate()), 10, 130);
        }

        private class ButtonHandler implements ActionListener {

            public void actionPerformed(ActionEvent event) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JButton button = (JButton) event.getSource();
                        if (button == buy_button) {
                            for (Component c : dashBoard_container.getComponents()){
                             Stock_card sc = (Stock_card) c;
                             sc.stop_thread();
                             dashBoard_container.remove(c);
                            }
                            interest_stock.removeIf(s->s.getId().equals(stock.getId()));
                            stockAPI.delete_stock_in_api(stock.getId());
                            db.delete_by_id(stock.getId());
                            repaint_dashboard();
                        }
                    }
                });
                thread.start();
            }
        }

        public String filter_price(){
            if (stock.getPrice()!=null)return stock.getPrice();
            return "抓不到資料TT";
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(card_width, card_height);
        }

    }
    class CustomerRendererPane extends JPanel implements ListCellRenderer {
        private Color colors[] = { new Color(240, 240, 255), new Color(245, 245, 200) };
        private Border selectedBorder;
        private Border unselectedBorder;
        private Stock stock;
        private int index;
        public CustomerRendererPane() {
            this.setOpaque(true);
            this.setBorder(new EtchedBorder());
        }
        public void setCustomer(Stock s) {
            stock = s;
        }
        public void setIndex(int i) {
            index = i;
        }
        public void setSelected(boolean isSelected) {
            if (isSelected) {
                if (selectedBorder == null) {
                    // set selecedBorder when first executed
                    selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,buy_stock_list.getSelectionBackground());
                }
                this.setBorder(selectedBorder);
            } else { // unselect
                // set unselecedBorder when first executed
                if (unselectedBorder == null) {
                    unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5, buy_stock_list.getBackground());
                }
                setBorder(unselectedBorder);
            }
        }
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            //ø�scell�I����
            g.setColor(colors[index % 2]);
            g.fillRect(0, 0, 330, 200);

            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 30));
            g.drawString(stock.getId(), 10, 40);
            g.drawString(stock.getName(), 10, 70);
            g.setFont(new Font("Time New Romans", Font.PLAIN, 28));
            g.drawString("股價:"+stock.getPrice(),10,100);
            g.setColor(Color.BLACK);
            SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
            g.drawString("時間:"+sdf.format(stock.getDate()), 10, 130);
        }
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(330, 200); // �C��cell�j�p
        }
        public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean focus) {
            setSelected(isSelected); // �]�w������A(���Pø�s��k)
            setCustomer((Stock) value); //�ثe�Qø�s������
            setIndex(index); // �ثeø�s����blistModel��������
            return this;
        }
    }


    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        Template_frame frame = new Template_frame();
    }
}
