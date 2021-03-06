package game.state;

import game.Game;
import game.util.resource.ImageLibrary;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.gui.AbstractComponent;
import org.newdawn.slick.gui.ComponentListener;
import org.newdawn.slick.gui.MouseOverArea;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public class StateMenu extends BasicGameState implements ComponentListener {

    private Image buttonSingle; // buttonForeverAlone
    private Image buttonMulti; // buttonIHaveFriends
    private Image buttonServer; // buttonDoesntDoAnything
    private Image buttonOption; // buttonThisGameIsPerfect
    
    private MouseOverArea[] areas = new MouseOverArea[4];
    private Image background;
    
    public static final int AREA_SINGLEPLAYER = 0;
    public static final int AREA_MULTIPLAYER = 1;
    public static final int AREA_SERVER = 2;
    
    private StateBasedGame game;

    private int id;

    public StateMenu(int id) {
        this.id = id;
    }

    @Override
    public void init(GameContainer container,StateBasedGame game) throws SlickException { }
    
    @Override
    public void enter(GameContainer container, StateBasedGame game) {
        this.game = game;
        
        buttonSingle = ImageLibrary.BUTTON_SINGLE.getImage();
        buttonMulti = ImageLibrary.BUTTON_MULTI.getImage();
        buttonServer = ImageLibrary.BUTTON_SERVER.getImage();
        buttonOption = ImageLibrary.BUTTON_OPTION.getImage();
        background = ImageLibrary.BACKGROUND.getImage();
        
        areas[0] = new MouseOverArea(container,buttonSingle,356,118,200,90,this);
        areas[1] = new MouseOverArea(container,buttonMulti,356,206,200,90,this);
        areas[2] = new MouseOverArea(container,buttonServer,356,294,200,90,this);
        areas[3] = new MouseOverArea(container,buttonOption,356,382,200,90,this);
        
        for (int i=0;i<4;i++) {
            areas[i].setNormalColor(new Color(1,1,1,1f));
            areas[i].setMouseOverColor(new Color(1,1,1,0.3f));
        }
    }
    
    @Override
    public void render(GameContainer container,StateBasedGame game,Graphics g) throws SlickException {
        if (game.getCurrentState().getID() != id)
            return;
        
        background.draw(0,0,640,512);

        for (int i=0;i<4;i++)
            areas[i].render(container,g);
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        if (game.getCurrentState().getID() != id)
            return;
    }
    
    @Override
    public void keyPressed(int key, char c) {
        if (key == Input.KEY_ENTER)
            game.enterState(Game.STATE_SINGLEPLAYER);
    }
    
    @Override
    public void componentActivated(AbstractComponent source) {
        if (source==areas[AREA_SINGLEPLAYER]) {
            game.enterState(Game.STATE_SINGLEPLAYER);
        } else if (source==areas[AREA_MULTIPLAYER]) {
            game.enterState(Game.STATE_SERVER_SELECT);
        } else if (source==areas[3]) {
            //game.enterState(Game.STATE_OPTIONS);
        } 
    }

    @Override
    public int getID() {
        return id;
    }
}
