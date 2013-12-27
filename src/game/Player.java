package game;

import org.lwjgl.input.Mouse;
import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

public class Player {
    
    private static EntitySprite sprite;

    private static Animation sword;

    private static int spritePointer;
    
    private static double x = 64;
    private static double y = 64;
    private static final double speed = 0.1;
    
    private static Options keybind;
    
    private static boolean DnHl;
    private static boolean UpHl;
    private static boolean LfHl;
    private static boolean RiHl;
    private static boolean DnPr;
    private static boolean UpPr;
    private static boolean LfPr;
    private static boolean RiPr;
    
    private static final int swordDuration = 48;
    
    private static int direction;
    
    private static boolean attacking;
    private static int attackTimer;
    private static int attackDelay;
    
    public static void init(GameContainer container, Options options) throws SlickException {
        initializeSprite();
        spritePointer = 3;
        keybind = options;
        attacking = false;
        attackDelay = 0;
    }
    
    public static void update(GameContainer container, int delta) {
        movePlayer(container.getInput(), delta);
        resolveAttack(container.getInput(), delta, container.getHeight());
    }
    
    public static void render(GameContainer container, Graphics g) throws SlickException {
        Animation currentSprite = sprite.getAnim(spritePointer);
        Input input = container.getInput();
        if (SlickGame.DEBUG_MODE) {
            g.drawString((DnHl?"dh ":"")
                    + (UpHl?"uh ":"")
                    + (LfHl?"lh ":"")
                    + (RiHl?"rh ":"")
                    + (DnPr?"dp ":"")
                    + (UpPr?"up ":"")
                    + (LfPr?"lp ":"")
                    + (RiPr?"rp ":""), 50, 50);
            g.drawString(String.valueOf(direction), 50, 75);
            g.drawString("x: " + String.valueOf(x), 50, 100);
            g.drawString("y: " + String.valueOf(y), 50, 125);
            g.drawString("mx: " + Mouse.getX(), 50, 150);
            g.drawString("my: " + Mouse.getY(), 50, 175);
            g.drawString("dx: " + String.valueOf(Mouse.getX()-x), 50, 200);
            g.drawString("dy: " + String.valueOf(Mouse.getY()+y-container.getHeight()), 50, 225);
            g.drawString(attacking?"Attacking":"Not attacking",50,250);
            g.drawString(String.valueOf(attackTimer),50,275);
            g.drawString(String.valueOf(sword.getFrame()),50,300);
        }
        currentSprite.draw((int)(x*4)-32,(int)(y*4)-32,64,64);
        if (attacking) {
            sword.draw((int)(x*4)-96,(int)(y*4)-96,192,192);
        }
    }
    
    public static void resolveAttack(Input input, int delta, int height) {
        if ((input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON) && keybind.MOUSE_ATTACK)
                || (input.isKeyDown(keybind.A_UP)
                    ||input.isKeyDown(keybind.A_DOWN)
                    ||input.isKeyDown(keybind.A_LEFT)
                    ||input.isKeyDown(keybind.A_RIGHT))
                && !attacking && attackDelay < 1) {
            double dy = y+Mouse.getY()-height;
            double dx = Mouse.getX()-x;
            //getMouseDirection(dx, dy);
            getKeyboardDirection(input);
            direction = (direction+6)%8;
            attack(direction);
        }
        attackTimer+=delta;
        attackDelay-=delta;
        if (attackTimer > swordDuration*4.5) {
            attacking = false;
        } else if (sword.getFrame()==(direction+10)%8) {
            sword.stop();
        }
    }
    
    public static void getKeyboardDirection(Input input) {
        if (input.isKeyDown(keybind.A_RIGHT))
            direction = 0;
        else if (input.isKeyDown(keybind.A_UP))
            direction = 2;
        else if (input.isKeyDown(keybind.A_LEFT))
            direction = 4;
        else if (input.isKeyDown(keybind.A_DOWN))
            direction = 6;
    }
    
    public static void getMouseDirection(double dx, double dy) {
        if (dx>=0) {
            if (dy>0) {
                if (dx>=dy)
                    direction = 0;
                else
                    direction = 1;
            }
            else {
                if (dx>=-dy)
                    direction = 7;
                else
                    direction = 6;
            }
        } else {
            if (dy>0) {
                if (-dx>=dy)
                    direction = 3;
                else
                    direction = 2;
            }
            else {
                if (-dx>=-dy)
                    direction = 4;
                else
                    direction = 5;
            }
        }
    }
    
    public static void attack(int direction) {
        attacking = true;
        attackTimer = 0;
        attackDelay = sword.getDuration(0)*2 + 500;
        sword.restart();
        sword.setCurrentFrame(direction);
    }
    
    public static void movePlayer(Input input, int delta) {
        DnHl = input.isKeyDown(keybind.M_DOWN);
        DnPr = input.isKeyPressed(keybind.M_DOWN);
        UpHl = input.isKeyDown(keybind.M_UP);
        UpPr = input.isKeyPressed(keybind.M_UP);
        LfHl = input.isKeyDown(keybind.M_LEFT);
        LfPr = input.isKeyPressed(keybind.M_LEFT);
        RiHl = input.isKeyDown(keybind.M_RIGHT);
        RiPr = input.isKeyPressed(keybind.M_RIGHT);
        
        if ((DnHl || DnHl) && (UpHl || UpHl)) {
            UpHl = false;
            DnHl = false;
        }
        
        if ((LfHl || LfPr) && (RiHl || RiPr)) {
            LfHl = false;
            RiHl = false;
        }
        
        if (DnHl) {
            spritePointer = 3;
        } else {
            sprite.getAnim(3).stop();
        }
        if (DnHl) {
            sprite.getAnim(3).start();
            y += speed*delta;
            if (!UpHl && !LfHl && !RiHl) {
                spritePointer = 3;
            }
        } else {
            sprite.getAnim(3).setCurrentFrame(1);
        }
        
        if (RiPr) {
            spritePointer = 0;
        } else {
            sprite.getAnim(0).stop();
        }
        if (RiHl) {
            sprite.getAnim(0).start();
            x += speed*delta;
            if (!UpHl && !LfHl && !DnHl) {
                spritePointer = 0;
            }
        } else {
            sprite.getAnim(0).setCurrentFrame(1);
        }
        
        if (UpPr) {
            spritePointer = 1;
        } else {
            sprite.getAnim(1).stop();
        }
        if (UpHl) {
            sprite.getAnim(1).start();
            y -= speed*delta;
            if (!DnHl && !LfHl && !RiHl) {
                spritePointer = 1;
            }
        } else {
            sprite.getAnim(1).setCurrentFrame(1);
        }
        
        if (LfPr) {
            spritePointer = 2;
        } else {
            sprite.getAnim(2).stop();
        }
        if (LfHl) {
            sprite.getAnim(2).start();
            x -= speed*delta;
            if (!UpHl && !DnHl && !RiHl) {
                spritePointer = 2;
            }
        } else {
            sprite.getAnim(2).setCurrentFrame(1);
        }
    }
    
    private static void initializeSprite() throws SlickException {
        sprite = new EntitySprite();
        sprite.setAnimations(                
                ResourceLoader.initializeAnimation("player_right.png",166),
                ResourceLoader.initializeAnimation("player_backward.png",166),
                ResourceLoader.initializeAnimation("player_left.png",166),
                ResourceLoader.initializeAnimation("player_forward.png",166)
        );
        sprite.setMasks(
                initializeMask(0),
                initializeMask(1),
                initializeMask(2),
                initializeMask(3)
        );
        sword = ResourceLoader.initializeAnimation("sword_slash.png",41,48);
        sword.stop();
    }
    
    private static AnimationMask initializeMask(int index) {
        EntityMask[] masks = new EntityMask[4];
        for (int i=0;i<4;i++) {
            masks[i] = new EntityMask(sprite
                    .getAnim(index)
                    .getImage(i));
        }
        return new AnimationMask(masks);
    }
}