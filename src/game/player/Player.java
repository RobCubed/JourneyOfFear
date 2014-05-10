package game.player;

import game.enemy.Enemy;
import game.environment.obstacle.Obstacle;
import game.environment.obstacle.Tree;
import game.hud.HUD;
import game.hud.Minimap;
import game.map.Area;
import game.network.server.DataPacket;
import game.network.server.EnemyPlayerData;
import game.player.attack.Attack;
import game.player.attack.AttackAxeCleave;
import game.player.attack.AttackDaggerSlash;
import game.player.attack.AttackSwordSlash;
import game.sprite.AnimationMask;
import game.sprite.EntitySprite;
import game.sprite.Hittable;
import game.sprite.ImageMask;
import game.sprite.Rectangle;
import game.state.StateMultiplayer;
import game.state.StateSingleplayer;
import game.util.GameObject;
import game.util.Options;
import game.util.resource.AnimationLibrary;
import game.util.resource.ImageLibrary;
import game.util.resource.SoundLibrary;
import java.util.ArrayList;
import java.util.List;
import org.newdawn.slick.Animation;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

public class Player extends GameObject implements Hittable {

    private EntitySprite sprite;
    
    private List<Attack> attacks;
    private Attack attack;
    private int attackIndex;
        
    private int spritePointer;
    
    private int x = 640;
    private int y = 512;
    private final double speed = 0.5;
    private int spriteHeight;
    private int spriteWidth;
    
    private Rectangle collisionMask = new Rectangle(x-spriteWidth/2,y-spriteHeight/2,x+spriteWidth/2,y+spriteHeight/2);
    
    private int camX;
    private int camY;
    private int delta;
    
    private int MAX_HEALTH = 40;
    private int currentHealth = MAX_HEALTH;
    
    private static Image[] hearts;
    
    private Area area;
    private List<HUD> hud;
    
    private int attackDirection;
    
    private boolean damageBlink;
    private boolean invulnerable = false;
    private int invulnerabilityTimer = 0;
    private int stunTimer;
    private int knockbackDX;
    private int knockbackDY;
    private final int DAMAGE_BLINK_TIME = 200;
    private final int KNOCKBACK_DISTANCE = 200;
    private final int STUN_DURATION = 400;
    
    // How slippery knockback is. Less means more slide.
    private final int KNOCKBACK_MULTIPLIER = 30;
    private final int INVULNERABILITY_DURATION = DAMAGE_BLINK_TIME*3;
    
    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    @Override public int getDepth() { return y; }
    
    private static final Color COLOR = Color.green;
    
    @Override
    public Color getColor() {
        return COLOR;
    }
    
    @Override
    public ImageMask getCollisionMask() {
        return sprite.getAnimationMask(spritePointer)
                .getImageMask(sprite.getAnim(spritePointer).getFrame()).update(x-spriteWidth/2,y-spriteHeight/2);
    }

    public ImageMask getAttackMask() {
        return attack.getMask(x-spriteWidth/2,y-spriteHeight/2);
    }
    
    public byte[] getBytes(int id) {
        DataPacket packet = new DataPacket(new EnemyPlayerData(id,x,y));
        return packet.getBytes();
    }
    
    public Attack getAttack() {
        return attack;
    }
    
    public int getAttackIndex() {
        return attackIndex;
    }
    
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    
    public Player() {
        attacks = new ArrayList<Attack>();
        attacks.add(AttackAxeCleave.create());
        attacks.add(AttackSwordSlash.create());
        attacks.add(AttackDaggerSlash.create());
        attackIndex = 0;
        attack = attacks.get(0);
        hud = new ArrayList<HUD>();
    }
    
    public void init(GameContainer container) throws SlickException {
        initializeSprite();
        hearts = new Image[5];
        hearts[0] = ImageLibrary.HEART_0.getImage();
        hearts[1] = ImageLibrary.HEART_1.getImage();
        hearts[2] = ImageLibrary.HEART_2.getImage();
        hearts[3] = ImageLibrary.HEART_3.getImage();
        hearts[4] = ImageLibrary.HEART_4.getImage();
        spritePointer = 3;
        spriteHeight = sprite.getAnim(spritePointer).getHeight() * 4;
        spriteWidth = sprite.getAnim(spritePointer).getWidth() * 4;
        attack.init();
        hud.add(new Minimap());
    }
    
    public void update(GameContainer container, int delta) {
        this.delta = delta;
        resolveInvulnerability(delta); //and knockback
        movePlayer(container.getInput(),delta);
        resolveCollision();
        collisionMask.set(x-spriteWidth/2,y-spriteHeight/2,x+spriteWidth/2,y+spriteHeight/2);
        Input input = container.getInput();
        
        if (input.isKeyPressed(Options.SWITCH_WEAPON.key())) {
            attackIndex = (attackIndex+attacks.size()+1)%attacks.size();
            attack = attacks.get(attackIndex);
            attack.init();
        }
        if ((input.isKeyDown(Options.ATTACK_UP.key())
                || input.isKeyDown(Options.ATTACK_DOWN.key())
                || input.isKeyDown(Options.ATTACK_LEFT.key())
                || input.isKeyDown(Options.ATTACK_RIGHT.key()))
                && attack.canAttack() && !invulnerable) {
            getAttackDirection(input);
            attack.attack(attackDirection,true);
        }
        attack.update(delta,x-spriteWidth/2,y-spriteHeight/2);
        for (Enemy e : area.getEnemies())
            attack.resolveAttackHit(e,x-spriteWidth/2,y-spriteHeight/2);
    }
    
    @Override
    public void render(Graphics g) {
        Animation currentSprite = sprite.getAnim(spritePointer);
        currentSprite.draw(x-spriteWidth/2,y-spriteHeight/2,64,64,damageBlink?Color.red:Color.white);
        renderHealth();
        attack.render(x-spriteWidth/2,y-spriteHeight/2);
        if (StateSingleplayer.DEBUG_MODE)
            renderDebugInfo(g);
    }
    
    public void renderHealth() {
        int fullNum = currentHealth/4;
        int partNum = currentHealth%4;
        int emptyNum = 10-(fullNum+partNum);
        for (int i=0;i<MAX_HEALTH/4;i++) {
            if (i<fullNum) {
                hearts[4].draw(camX+(600-i*30),camY+10,4);
            } else if (partNum>0) {
                hearts[partNum].draw(camX+(600-i*30),camY+10,4);
                partNum=0;
            } else if (i-(fullNum+currentHealth%4)<emptyNum) {
                hearts[0].draw(camX+(600-i*30),camY+10,4);
            }
        }
    }
    
    public void renderHUD(Graphics g) {
        hud.get(0).display(g,this,area,camX,camY);
    }
    
    public void setArea(Area newArea) {
        area = newArea;
    }
    
    public void updateViewPort(int camX, int camY) {
        this.camX = camX;
        this.camY = camY;
    }
    
    private void initializeSprite() {
        sprite = new EntitySprite(4);
        
        Animation[] animList = {
            AnimationLibrary.PLAYER_RIGHT.getAnim(),
            AnimationLibrary.PLAYER_UP.getAnim(),
            AnimationLibrary.PLAYER_LEFT.getAnim(),
            AnimationLibrary.PLAYER_DOWN.getAnim()
        };
        sprite.setAnimations(animList);
        
        AnimationMask[] animMaskList = {
            initializeMask(0),
            initializeMask(1),
            initializeMask(2),
            initializeMask(3)
        };
        sprite.setMasks(animMaskList);
    }
    
    private AnimationMask initializeMask(int index) {
        int frames = sprite.getAnim(index).getFrameCount();
        ImageMask[] masks = new ImageMask[frames];
        for (int i=0;i<frames;i++) {
            masks[i] = new ImageMask(sprite.getAnim(index).getImage(i),x-spriteWidth/2,y-spriteHeight/2);
        }
        return new AnimationMask(masks);
    }
    
    private void movePlayer(Input input, int delta) {
        if (stunTimer>0) {
            sprite.getAnim(spritePointer).setCurrentFrame(1);
            sprite.getAnim(spritePointer).stop();
            x+=(knockbackDX*stunTimer)/(KNOCKBACK_DISTANCE*KNOCKBACK_MULTIPLIER);
            y+=(knockbackDY*stunTimer)/(KNOCKBACK_DISTANCE*KNOCKBACK_MULTIPLIER);
            return;
        }
        
        boolean DnHl = input.isKeyDown(Options.MOVE_DOWN.key());
        boolean DnPr = input.isKeyPressed(Options.MOVE_DOWN.key());
        boolean UpHl = input.isKeyDown(Options.MOVE_UP.key());
        boolean UpPr = input.isKeyPressed(Options.MOVE_UP.key());
        boolean LfHl = input.isKeyDown(Options.MOVE_LEFT.key());
        boolean LfPr = input.isKeyPressed(Options.MOVE_LEFT.key());
        boolean RiHl = input.isKeyDown(Options.MOVE_RIGHT.key());
        boolean RiPr = input.isKeyPressed(Options.MOVE_RIGHT.key());
        
        if ((DnHl || DnPr) && (UpHl || UpPr)) {
            UpHl = false;
            DnHl = false;
        }
        
        if ((LfHl || LfPr) && (RiHl || RiPr)) {
            LfHl = false;
            RiHl = false;
        }
        
        int dx = 0;
        int dy = 0;
        
        if (DnPr)
            spritePointer = 3;
        else
            sprite.getAnim(3).stop();
        if (DnHl) {
            sprite.getAnim(3).start();
            dy += 1;
            if (!UpHl && !LfHl && !RiHl)
                spritePointer = 3;
        } else
            sprite.getAnim(3).setCurrentFrame(1);
        
        if (RiPr)
            spritePointer = 0;
        else
            sprite.getAnim(0).stop();
        if (RiHl) {
            sprite.getAnim(0).start();
            dx += 1;
            if (!UpHl && !LfHl && !DnHl)
                spritePointer = 0;
        } else
            sprite.getAnim(0).setCurrentFrame(1);
        
        if (UpPr)
            spritePointer = 1;
        else
            sprite.getAnim(1).stop();
        if (UpHl) {
            sprite.getAnim(1).start();
            dy -= 1;
            if (!DnHl && !LfHl && !RiHl)
                spritePointer = 1;
        } else
            sprite.getAnim(1).setCurrentFrame(1);
        
        if (LfPr)
            spritePointer = 2;
        else
            sprite.getAnim(2).stop();
        if (LfHl) {
            sprite.getAnim(2).start();
            dx -= 1;
            if (!UpHl && !DnHl && !RiHl)
                spritePointer = 2;
        } else
            sprite.getAnim(2).setCurrentFrame(1);
        
        int steps = (int) (delta*speed);
        int actualSteps = 0;
        
        for (Obstacle o : area.getObstacles()) {
            if (!(o instanceof Tree))
                continue;
            Tree tree = (Tree) o;
            int newSteps = tree.canMoveSteps(collisionMask,steps,dx,dy);
            if (newSteps>actualSteps)
                actualSteps = newSteps;
        }
        
        x += actualSteps*dx;
        y += actualSteps*dy;
    }
    
    private void getAttackDirection(Input input) {
        if (input.isKeyDown(Options.ATTACK_RIGHT.key()))
            attackDirection = 0;
        else if (input.isKeyDown(Options.ATTACK_UP.key()))
            attackDirection = 2;
        else if (input.isKeyDown(Options.ATTACK_LEFT.key()))
            attackDirection = 4;
        else if (input.isKeyDown(Options.ATTACK_DOWN.key()))
            attackDirection = 6;
    }
    
    private void resolveCollision() {
        for (Enemy e : area.getEnemies()) {
            if(getCollisionMask().intersects(e.getCollisionMask()))
                resolveHit(e.getX(),e.getY(),e.getHitDamage());
        }
    }
    
    public void resolveHit(int ox, int oy) {
        resolveHit(ox,oy,-1,1);
    }
    
    public void resolveHit(int ox, int oy, int damage) {
        resolveHit(ox,oy,-1,damage);
    }
    
    public void resolveHit(int ox, int oy, int attackId, int damage) {
        resolveHit(ox,oy,attackId,damage,1);
    }
    
    @Override
    public void resolveHit(int ox, int oy, int attackId, int damage, double mult) {
        if (!invulnerable) {
            invulnerable = true;
            invulnerabilityTimer = INVULNERABILITY_DURATION;
            initializeKnockback(x-ox,y-oy,mult);
            currentHealth -= damage;
            SoundLibrary.SWORD_HIT.play();
        }
    }
    
    private void initializeKnockback(int dx, int dy) {
        initializeKnockback(dx,dy,1);
    }
    
    private void initializeKnockback(int dx, int dy, double mult) {
        if (stunTimer<=0) {
            knockbackDX=(int)(mult*KNOCKBACK_DISTANCE*Math.cos(Math.atan2(dy,dx)));
            knockbackDY=(int)(mult*KNOCKBACK_DISTANCE*Math.sin(Math.atan2(dy,dx)));
            stunTimer = STUN_DURATION;
        }
    }
    
    private void resolveInvulnerability(int delta) {
        invulnerabilityTimer -= delta;
        if (invulnerabilityTimer<1 && (invulnerabilityTimer/DAMAGE_BLINK_TIME)%2 == 0) {
            invulnerable = false;
            invulnerabilityTimer = 0;
        }
        if (stunTimer>0)
            stunTimer -= delta;
        damageBlink = false;
        if (invulnerable)
            damageBlink = (invulnerabilityTimer/DAMAGE_BLINK_TIME)%2 == 0;
    }
    
    private void renderDebugInfo(Graphics g) {
        g.setColor(Color.white);
        g.drawString("delta: " + String.valueOf(delta),10+camX,24+camY);
        g.drawString("x: " + String.valueOf(x),10+camX,38+camY);
        g.drawString("y: " + String.valueOf(y),10+camX,52+camY);
        attack.renderDebugInfo(camX+10,camY+66,g);
        if (StateMultiplayer.DEBUG_COLLISION) {
            getCollisionMask().render(g);
            collisionMask.render(g);
            attack.renderMask(x-spriteWidth/2,y-spriteHeight/2,g);
        }
        g.drawRect(x-8,y-8,16,16);
    }
}
