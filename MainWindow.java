package source;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.io.FilenameUtils;

public class MainWindow extends JFrame{
	
	private static final long serialVersionUID = 1L;
	
	public MainWindow(int windowWidth, int windowHeight){
		
		//image wrapper for holding image of the label
		final ImageWrapper imageWrapper = new ImageWrapper();
		//filters
		final Filters filters = new Filters();
				
		//set title and size of main window
		setTitle("PhotoCartoonizer");
		setSize(windowWidth, windowHeight);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		//BorderLayout as main layout
		BorderLayout mainLayout = new BorderLayout();
		//BorderLayout for image
		BorderLayout imageLayout = new BorderLayout();
		//GridLayout for buttons
		GridLayout buttonsLayout = new GridLayout(3, 1);
		
		//JPanel as main panel
		JPanel mainPanel = new JPanel(mainLayout);
		add(mainPanel);
		
		//JPanel for displaying image
		JPanel imagePanel = new JPanel(imageLayout);
		mainPanel.add(imagePanel, BorderLayout.CENTER);
		
		//JPanel for displaying buttons
		JPanel buttonsPanel = new JPanel(buttonsLayout);
		mainPanel.add(buttonsPanel, BorderLayout.PAGE_END);
		
		//JLabel for displaying image
		final JLabel imageLabel = new JLabel("   ");
		imagePanel.add(imageLabel, BorderLayout.CENTER);
		
		//button for opening image
		final JButton buttonOpenImage = new JButton("Open Image");
		buttonOpenImage.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				
				try{
					JFileChooser fileChooser = new JFileChooser();
					int fileChooserStatus = fileChooser.showOpenDialog(null);
					
					if(fileChooserStatus == JFileChooser.APPROVE_OPTION){
						//set image of imageWrapper and set icon of the label
						BufferedImage bufferedImage = ImageIO.read(fileChooser.getSelectedFile());
						imageWrapper.setImage(bufferedImage);
						imageLabel.setIcon(new ImageIcon(bufferedImage));						
						
						//set size of the frame according to the size of the image
						setSize(bufferedImage.getWidth() - 50, bufferedImage.getHeight());
					}
				}catch(Exception ex){
					System.out.println(ex.getMessage());
				}
				
								
			}
		});
		buttonsPanel.add(buttonOpenImage);
		
		//button for cartoonizing image
		final JButton buttonCartoonizeImage = new JButton("Cartoonize Image");
		buttonCartoonizeImage.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				
				BufferedImage bufferedImage = imageWrapper.getImage();
				if(bufferedImage != null){
					bufferedImage = filters.cartoonizeImage(bufferedImage);
					imageWrapper.setImage(bufferedImage);
					imageLabel.setIcon(new ImageIcon(bufferedImage));
				}
			}
		});
		buttonsPanel.add(buttonCartoonizeImage);
		
		//button for saving image
		final JButton buttonSaveImage = new JButton("Save Image");
		buttonSaveImage.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{
					JFileChooser fileChooser = new JFileChooser();
					int fileChooserStatus = fileChooser.showSaveDialog(null);
					
					if(fileChooserStatus == JFileChooser.APPROVE_OPTION){
						File outputFile = fileChooser.getSelectedFile();
						ImageIO.write(imageWrapper.getImage(), FilenameUtils.getExtension(outputFile.getAbsolutePath()), outputFile);
					}
				}catch(Exception ex){
					System.out.println(ex.getMessage());
				}
			}
		});
		buttonsPanel.add(buttonSaveImage);			
	}
	
	class ImageWrapper{
		BufferedImage image;
		
		ImageWrapper(){
			image = null;
		}
		
		BufferedImage getImage(){
			return image;
		}
		
		void setImage(BufferedImage image){
			this.image = image;
		}
	}

}
