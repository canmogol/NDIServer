import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ComboBoxAdapterExample extends JPanel {
    public ComboBoxAdapterExample() {
        JPanel panel = new JPanel(new FormLayout("p, 2dlu, p:g", "t:p, b:d:g"));
        panel.setBorder(new EmptyBorder(6, 6, 6, 6));

        List<User> userList = new ArrayList<User>() {{
            add(new User(1, "aa"));
            add(new User(2, "bb"));
        }};

        DefaultComboBoxModel userDefaultComboBoxModel = new DefaultComboBoxModel(new Vector<>(userList)){
            @Override
            public Object getElementAt(int index) {
                return ((User)super.getElementAt(index)).getName();
            }
        };

        CellConstraints cc = new CellConstraints();

        JComboBox comboBox = new JComboBox();
        comboBox.setModel(userDefaultComboBoxModel);

        comboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                System.out.println(comboBox.getModel().getSelectedItem());
                System.out.println(comboBox.getModel().getElementAt(0));
            }
        });

        panel.add(new JLabel("Combo Box:"), cc.xy(1, 1));
        panel.add(comboBox, cc.xy(3, 1));

        add(panel);
    }

    public static void main(String[] a) {
        JFrame f = new JFrame("ComboBox Adapter Example");
        f.setDefaultCloseOperation(2);
        f.add(new ComboBoxAdapterExample());
        f.pack();
        f.setVisible(true);
    }
}
