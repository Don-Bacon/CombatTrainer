import com.snowygryphon.osrs.script.*;
import com.snowygryphon.osrs.script_v2.geometry.Area;
import org.joml.Vector2i;

import javax.lang.model.element.Name;

import static com.snowygryphon.osrs.script.Skill.Attack;

@ScriptInfo(name="Combat Trainer", author = "Sensi", version = "0.2")
public class CombatTrainer extends Script {
   @Override public void onStart() {
      if(!bot.isLoggedIn()) { login_handler(); }
      setTarget();
      state = State.GoToTarget;
   }
   @Override public void update() {
      if(bot.isLoggedIn()) {
         setTarget();
         setEquipment();
         camera_movements();
         hop_world();
         switch (state) {
            case Attack: update_Attack(); break;
            case GoToTarget: update_GoToTarget(); break;
         }
      }
      else { login_handler(); }

      bot.sleep(500);
   }
   private void update_GoToTarget() {
      if (getDistance(pos.getTile()) < 2) { state = State.Attack; }

      if(getDistance(pos.getTile()) >= 12) {
         bot.webWalkTo(pos);
         bot.sleep(5_000 + bot.getRandom().nextInt(2_000));
      }
      if(getDistance(pos.getTile()) < 12) {
         bot.walkTowardsTileOnMinimap(pos.getTile());
         bot.sleep(5_000 + bot.getRandom().nextInt(2_000));
      }
   }
   private void update_Attack() {
      target = bot.getNPCs().getClosest(targetName);
      if (target != null && area.contains(target)) {
         if (bot.getLocalPlayer().getAttackTarget() == null && target.getAttackTarget() == null) {
            bot.logInfo("Interacting with target");
            bot.interactWith(target, "Attack");
         }
         else {
            bot.logInfo("waiting");
            bot.sleep(1_000 + bot.getRandom().nextInt(500));
         }
      }
      else { state = State.GoToTarget; }
   }
   @Override public void onStop() {}

   private void login_handler() {
      Vector2i ok = new Vector2i(500, 600);
      Vector2i welcome_screen = new Vector2i(490, 600);
      if (!bot.isLoggedIn()) {
         bot.tap(ok);
         bot.sleep(bot.getRandom().nextInt(100) + 3_000);

         bot.clickLoginButton();
         bot.sleep(bot.getRandom().nextInt(100) + 10_000);
         if (bot.getStdWindows().getWelcomeWindow().isOpen()) {
            //bot.logInfo("tap enter");
            bot.tap(welcome_screen);
         }
      }
   }
   private void setTarget() {
      int attack = bot.getLocalPlayer().getSkills().get(Skill.Attack).getCurrent();
      int strength = bot.getLocalPlayer().getSkills().get(Skill.Strength).getCurrent();
      int defence = bot.getLocalPlayer().getSkills().get(Skill.Defence).getCurrent();
      if (attack < 20 || strength < 20 || defence < 20) {
         bot.logInfo("set to chickens");
         targetName = "Chicken";
         area = area_chickens;
         pos = pos_chickens;
      }
      else if (attack >= 20 && strength >= 20 && defence >= 20) {
         bot.logInfo("set to cows");
         targetName = "Cow";
         area = area_cows;
         pos = pos_cows;
      }
   }
   private double getDistance(Tile target) {
      Tile lp_tile = bot.getLocalPlayer().getTile();
      return Math.hypot(target.x - lp_tile.x, target.y - lp_tile.y);
   }
   private void camera_movements() {
      Vector2i center_point;
      Vector2i end_point;
      if (System.currentTimeMillis() > last_time + 60000) {
            center_point = new Vector2i(bot.getRandom().nextInt(600) + 100, bot.getRandom().nextInt(650) + 350);
            end_point = new Vector2i(bot.getRandom().nextInt(1000), bot.getRandom().nextInt(1000));
            bot.swipe(center_point, end_point);
            last_time = System.currentTimeMillis();
      }
   }
   private void hop_world() {
      int random = bot.getRandom().nextInt(58);

      int[] f2p_worlds = {301, 308, 316, 326, 335, 371, 379, 380, 382, 383,
              384, 394, 397, 398, 399, 417, 418, 425, 426, 430, 431, 433,
              434, 435, 436, 437, 451, 452, 453, 454, 455, 456, 469, 470,
              471, 472, 473, 475, 476, 483, 497, 498, 545, 546, 547, 552,
              553, 554, 555, 556, 562, 563, 564, 565, 566, 567, 571, 573, 574}; // 59 f2p worlds

      int[] members_worlds = {327,328,329,330,331,332,333,334,336,337,
              338,339,340,341,342,343,344,346,347,348,
              350,351,352,354,355,356,357,358,359,360,
              362,365,367,368,369,370,374,375,376,377,
              378,386,387,388,389,390,395,}; // 47 mem worlds
      int next_world = f2p_worlds[random];

      worldHoppingInterval = 1_800_000/*30minutes*/ + bot.getRandom().nextInt(7_200_000)/*2hours*/;
      if (System.currentTimeMillis() > lastWorldHop + worldHoppingInterval) {
         bot.logInfo("hopping to world: " + next_world);
         bot.getWorldSwitcher().switchToWorld(next_world);
         bot.sleep(bot.getRandom().nextInt(1_000) + 5_000);
      }
   }
   private void setEquipment() {
      int attack = bot.getLocalPlayer().getSkills().get(Skill.Attack).getCurrent();
      int defence = bot.getLocalPlayer().getSkills().get(Skill.Defence).getCurrent();

      if (attack < 5 && bot.getInventory().getItemWithName("Steel scimitar") != null) {
         //bot.interactWith(bot.getInventory().getItemWithName("Iron scimitar"), );
      }
      else if (attack == 5 && bot.getInventory().getItemWithName("Steel scimitar") != null) { // if level 5 and inventory has steel scimitar
         //bot.interactWith(bot.getInventory().getItemWithName("Steel scimitar"), );
      }
         // equip steel scimitar
      // if level 20 and inventory has mithril scimitar
         // equip mithril scimitar
      // if level 30 and inventory has adamant scimitar
         // equip adamant scimitar
      // if level 40 and inventory has rune scimitar
   }

   private enum State { Attack, GoToTarget}
   private State state;
   private String targetName;
   private NPC target;
   private Area area;
   private Area area_chickens = new Area(3172, 3299, 3183, 3290);
   private Area area_cows = new Area(3194, 3300, 3210, 3285);
   private WorldPos pos;
   private WorldPos pos_chickens = new WorldPos(3178, 3295, 0);
   private WorldPos pos_cows = new WorldPos(3201, 3292, 0);
   private long last_time = System.currentTimeMillis();
   private long lastWorldHop = System.currentTimeMillis();
   private long worldHoppingInterval;
}
