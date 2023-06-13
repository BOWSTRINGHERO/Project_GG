package game;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;




public class Game extends JPanel implements ActionListener, KeyListener {

    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 600;
    private static final int PLAYER_WIDTH = 50;
    private static final int PLAYER_HEIGHT = 50;
    private static final int MONSTER_WIDTH = 50;
    private static final int MONSTER_HEIGHT = 50;
    public static final int PROJECTILE_WIDTH = 10;
    public static final int PROJECTILE_HEIGHT = 10;
    private static final int PLAYER_PROJECTILE_SPEED = 5;
    private static final int MONSTER_PROJECTILE_SPEED = 3;
    public static final int MONSTER_PROJECTILE_COOLDOWN = 100;

    private Item item;
    private Timer itemTimer;
    private boolean isItemActive;
    private Player player;
    private Monster monster;
    private ArrayList<Projectile> playerProjectiles;
    private ArrayList<Projectile> monsterProjectiles;
    private Timer timer;
    public static int playerHealth;
    private int monsterHealth;
    private ArrayList<Image> playerHealthImages;
    private Image playerHealthImage;

    private boolean upPressed;
    private boolean downPressed;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean spacePressed;
    private int lastPressedDirection;


    private Item key;
    private boolean isMonsterAlive;
    private boolean keyCard;
    private String mImage;

    private Timer monsterAttackTimer;  // 몬스터 공격 타이머
    private int projectileCount;
    private int stage;

    public Game() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        monsterAttackTimer = new Timer(1000, this);  // 1초마다 타이머 이벤트 발생
        monsterAttackTimer.start();
        projectileCount = 0;

        playerHealth = 10000;
        monsterHealth = 300;
        stage=1;

        player = new Player(SCREEN_WIDTH / 2 - PLAYER_WIDTH / 2, SCREEN_HEIGHT - PLAYER_HEIGHT - 10, PLAYER_WIDTH, PLAYER_HEIGHT);
        monster = new Monster(SCREEN_WIDTH / 2 - MONSTER_WIDTH / 2, 10, MONSTER_WIDTH, MONSTER_HEIGHT);
        playerProjectiles = new ArrayList<>();
        monsterProjectiles = new ArrayList<>();
        timer = new Timer(10, this);
        timer.start();

        itemTimer = new Timer(5000, this); // 5초마다 타이머 이벤트 발생
        itemTimer.setInitialDelay(0); // 초기 딜레이를 5초로 설정하여 처음 아이템 생성
        itemTimer.start();
        isItemActive = false;

        isMonsterAlive = true;
        keyCard = false;

        key = new Item(100, 100, 40, 40);

        mImage = "images/M.png";

        playerHealthImages = new ArrayList<>();
        String imagePath = "images/player_hp.png";
        try {
            playerHealthImage = ImageIO.read(new File("images/player_0hp.png"));
        } catch (IOException e) {
        }
        for (int i = 0; i <10; i++) { //하트 10개 생성
            try {
                Image healthImage = ImageIO.read(new File(imagePath));
                playerHealthImages.add(healthImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override

    public void actionPerformed(ActionEvent e) {
        player.move(upPressed, downPressed, leftPressed, rightPressed);
        monster.chasePlayer(player);

        if(isMonsterAlive) {
            if (stage == 1) {
                attack1();
            }else {
                attack2(e);
            }
        }

        for (int i = 0; i < playerProjectiles.size(); i++) {
            Projectile projectile = playerProjectiles.get(i);
            projectile.move();

            if (projectile.isOffScreen(SCREEN_HEIGHT)) {
                playerProjectiles.remove(i);
                i--;
            } else if (projectile.intersects(monster)) {
                playerProjectiles.remove(i);
                i--;
                if(monsterHealth > 0)
                    monsterHealth -= 50;
            }
        }

        for (int i = 0; i < monsterProjectiles.size(); i++) {
            Projectile projectile = monsterProjectiles.get(i);
            projectile.move();

            if (projectile.isOffScreen(SCREEN_HEIGHT)) {
                monsterProjectiles.remove(i);
                i--;
            } else if (projectile.intersects(player)) {
                monsterProjectiles.remove(i);
                i--;
                playerHealth -= 10;
                if (playerHealth <= 0) {
                    // 플레이어 격파, 게임 오버 로직 처리
                    JOptionPane.showMessageDialog(null, "패배하였습니다!");

                }

            }

        }
        if (e.getSource() == itemTimer) {
            if (!isItemActive) {
                item = new Item(SCREEN_WIDTH / 2-25, SCREEN_HEIGHT / 2- 25 , 15, 15);
                isItemActive = true;
            }
        }
        ///만약 몬스터의 체력이 0이면 키가 나온다
        if (monsterHealth == 0) {

            isMonsterAlive = false;
            keyCard = true;
            if(player.getBounds().intersects(key.getBounds())){
                //System.out.println("작동");
                keyCard = false;
                //JOptionPane.showMessageDialog(null, "스테이지 2");
                int answer = JOptionPane.showConfirmDialog(this, "다음 스테이지로 이동하시겠습니까?", "confirm", JOptionPane.YES_NO_OPTION);
                if (answer == 1) {
                    System.exit(0);
                } else {
                    stage++;
                    resetGame();
                }
                //System.exit(0);
            }
        }

        if(isItemActive) {
            checkItemCollision();
        }

        if (!isItemActive && itemTimer.getDelay() == 0) {
            int itemX = (int) (Math.random() * (SCREEN_WIDTH - item.getWidth()));
            int itemY = (int) (Math.random() * (SCREEN_HEIGHT - item.getHeight()));

            item = new Item(itemX, itemY, 15, 15);
            isItemActive = true;

            itemTimer.setDelay(5000);
            itemTimer.setInitialDelay(5000);
        }



        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);


        g.setColor(Color.WHITE);
        g.drawString("플레이어 체력: " + playerHealth, 10, 20);
        g.drawString("몬스터 체력: " + monsterHealth, SCREEN_WIDTH - 150, 20);

        player.draw(g, "images/P.png");
        if (isMonsterAlive) {
            monster.draw(g, mImage);
        }

        int healthImageWidth = playerHealthImages.get(0).getWidth(null);
        int healthImageHeight = playerHealthImages.get(0).getHeight(null);
        int maxHealthImages = 10;
        int x = 10;
        int y = 30;

        if (isItemActive) {
            item.draw(g, "images/player_hp.png"); // 아이템 이미지 경로를 적절히 수정해야 합니다.
        }
        if (monsterHealth == 0 && keyCard == true) {
            key.draw(g, "images/K.png");
        }


        for (int i = 0; i < maxHealthImages; i++) {
            if (i >= playerHealth / 10) {
                // 체력이 i * 20보다 작으면 player_hp.png 이미지로 채움
                g.drawImage(playerHealthImage, x, y,30,30, null);
            } else {
                // 체력이 i * 20 이상이면 player_0hp.png 이미지로 채움
                g.drawImage(playerHealthImages.get(i), x, y, 30,30,null);
            }
            x += 20; // 이미지 간격 조절
        }

        for (Projectile projectile : playerProjectiles) {
            projectile.draw(g, "images/A.png");
        }

        for (Projectile projectile : monsterProjectiles) {
            projectile.draw(g, "images/A.png");
        }
    }
    private void checkItemCollision() {
        if (player.getBounds().intersects(item.getBounds())) {
            //System.out.println("작동2");
            player.heal();
            isItemActive = false; // Deactivate the item
        }
    }


    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_UP) {
            upPressed = true;
        }
        if (keyCode == KeyEvent.VK_DOWN) {
            downPressed = true;
        }
        if (keyCode == KeyEvent.VK_LEFT) {
            leftPressed = true;
        }
        if (keyCode == KeyEvent.VK_RIGHT) {
            rightPressed = true;
        }
        if (keyCode == KeyEvent.VK_SPACE) {
            spacePressed = true;
            firePlayerProjectile();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_UP) {
            upPressed = false;
        }
        if (keyCode == KeyEvent.VK_DOWN) {
            downPressed = false;
        }
        if (keyCode == KeyEvent.VK_LEFT) {
            leftPressed = false;
        }
        if (keyCode == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
        if (keyCode == KeyEvent.VK_SPACE) {
            spacePressed = false;
        }
    }

    private void firePlayerProjectile() {
        int projectileX = (int) (player.getX() + player.getWidth() / 2 - PROJECTILE_WIDTH / 2);
        int projectileY = (int) (player.getY() + player.getHeight() / 2 - PROJECTILE_HEIGHT / 2);
        int dx = 0;
        int dy = 0;

        if (upPressed) {
            dy = -PLAYER_PROJECTILE_SPEED;
        } else if (downPressed) {
            dy = PLAYER_PROJECTILE_SPEED;
        } else if (leftPressed) {
            dx = -PLAYER_PROJECTILE_SPEED;
        } else if (rightPressed) {
            dx = PLAYER_PROJECTILE_SPEED;
        }

        if (dx != 0 || dy != 0) {
            Projectile projectile = new Projectile(projectileX, projectileY, dx, dy);
            playerProjectiles.add(projectile);
        }
    }

    public void resetGame() {
        upPressed = false;
        downPressed = false;
        rightPressed = false;
        leftPressed = false;

        player.x = SCREEN_WIDTH / 2 - PLAYER_WIDTH / 2;
        player.y = SCREEN_HEIGHT - PLAYER_HEIGHT - 10;
        monster.x = SCREEN_WIDTH / 2 - MONSTER_WIDTH / 2;
        monster.y = 10;

        playerHealth = 10000;
        monsterHealth = 300;

        playerProjectiles.clear();
        monsterProjectiles.clear();
        //mImage = "images/T.png";

        isMonsterAlive = true;
        keyCard = false;
        isItemActive = false;
        itemTimer.setDelay(0);

        itemTimer.restart();
        timer.restart();
    }

    public void attack1() {
        if (monster.getProjectileCooldown() == 0) {
            int direction = (int) (Math.random() * 4);
            int projectileX = (int) (monster.getX() + monster.getWidth() / 2 - PROJECTILE_WIDTH / 2);
            int projectileY = (int) (monster.getY() + monster.getHeight() / 2 - PROJECTILE_HEIGHT / 2);
            Projectile projectile = null;

            switch (direction) {
                case 0:  // Up
                    projectile = new Projectile(projectileX, projectileY, 0, -MONSTER_PROJECTILE_SPEED);
                    break;
                case 1:  // Down
                    projectile = new Projectile(projectileX, projectileY, 0, MONSTER_PROJECTILE_SPEED);
                    break;
                case 2:  // Left
                    projectile = new Projectile(projectileX, projectileY, -MONSTER_PROJECTILE_SPEED, 0);
                    break;
                case 3:  // Right
                    projectile = new Projectile(projectileX, projectileY, MONSTER_PROJECTILE_SPEED, 0);
                    break;
            }

            if (projectile != null) {
                monsterProjectiles.add(projectile);
                monster.resetProjectileCooldown();
            }
        } else {
            monster.decreaseProjectileCooldown();
        }
    }

    public void attack2(ActionEvent e) {
        if (e.getSource() == monsterAttackTimer) {
            if (projectileCount < 36) {  // 발사체 수가 36개 미만일 때에만 발사
                double angle = projectileCount * (360.0 / 36);  // 원형으로 발사체를 배치하기 위한 각도 계산
                double radians = Math.toRadians(angle);  // 각도를 라디안으로 변환
                double dx = Math.cos(radians) * MONSTER_PROJECTILE_SPEED;  // X축 이동량 계산
                double dy = Math.sin(radians) * MONSTER_PROJECTILE_SPEED;  // Y축 이동량 계산

                int projectileX = (int) (monster.getX() + monster.getWidth() / 2 - PROJECTILE_WIDTH / 2);
                int projectileY = (int) (monster.getY() + monster.getHeight() / 2 - PROJECTILE_HEIGHT / 2);

                for (int i = 0; i < 36; i++) {
                    Projectile projectile = new Projectile(projectileX, projectileY, dx, dy);
                    monsterProjectiles.add(projectile);

                    angle += 36; // 10도씩 증가하여 다음 발사체의 각도 계산
                    radians = Math.toRadians(angle);
                    dx = Math.cos(radians) * MONSTER_PROJECTILE_SPEED;
                    dy = Math.sin(radians) * MONSTER_PROJECTILE_SPEED;
                }

                projectileCount += 36;  // 발사체 수 증가
            } else {
                projectileCount = 0;
            }
        }
    }
}