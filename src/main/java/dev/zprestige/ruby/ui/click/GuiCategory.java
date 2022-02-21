package dev.zprestige.ruby.ui.click;

import dev.zprestige.ruby.Ruby;
import dev.zprestige.ruby.module.Category;
import dev.zprestige.ruby.module.Module;
import dev.zprestige.ruby.module.client.NewGui;
import dev.zprestige.ruby.setting.impl.ParentSetting;
import dev.zprestige.ruby.ui.click.setting.GuiSetting;
import dev.zprestige.ruby.ui.click.setting.impl.GuiColor;
import dev.zprestige.ruby.ui.click.setting.impl.GuiParent;
import dev.zprestige.ruby.util.AnimationUtil;
import dev.zprestige.ruby.util.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public class GuiCategory {
    public Category category;
    public int x, y, width, height, dragX, dragY, deltaY, targetAnim, animHeight;
    public boolean isDragging;
    public boolean isOpened = true;
    public ArrayList<GuiModule> newModuleArrayList = new ArrayList<>();

    public GuiCategory(Category category, int x, int y, int width, int height) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        deltaY = y;
        Ruby.moduleManager.getModulesInCategory(category).forEach(module -> newModuleArrayList.add(new GuiModule(module, x + 1, deltaY += (height + 1), width - 2, height)));
        Ruby.moduleManager.getModulesInCategory(category).forEach(module -> module.scrollY = 0);
    }

    public void dragScreen(int mouseX, int mouseY) {
        if (!isDragging)
            return;
        x = dragX + mouseX;
        y = dragY + mouseY;
        deltaY = y;
        for (GuiModule newModule : newModuleArrayList) {
            newModule.x = x + 1;
            newModule.y = newModule.module.scrollY + (deltaY += (height + 1));
        }
    }

    public void drawScreen(int mouseX, int mouseY) {
        {
            dragScreen(mouseX, mouseY);
            if (isInsideFull(mouseX, mouseY))
                setScroll();
        }
        {
            deltaY = y;
            newModuleArrayList.forEach(newModule -> {
                deltaY += height + 1;
                newModule.y = newModule.module.scrollY + deltaY;
                if (newModule.isOpened) {
                    for (GuiSetting newSetting : newModule.newSettings) {
                        if (!newSetting.isVisible())
                            continue;
                        deltaY += height + 1;
                        if (newSetting instanceof GuiColor && newSetting.getSetting().isOpen)
                            deltaY += 109;
                        if (newSetting instanceof GuiParent)
                            deltaY += ((ParentSetting) newSetting.getSetting()).getValue() ? 2 : 1;
                    }
                } else {
                    deltaY += (newModule.animDeltaY - (height + 1));
                }
            });
        }
        {
            RenderUtil.drawRect(x, y, x + width, y + height, NewGui.Instance.color.getValue().getRGB());
            Ruby.rubyFont.drawStringWithShadow(category.toString(), x + (width / 2f) - (Ruby.rubyFont.getStringWidth(category.toString()) / 2f), y + (height / 2f) - (Ruby.rubyFont.getHeight(category.toString()) / 2f), -1);
        }
        {
            RenderUtil.drawRect(x, y + height, x + width, animHeight, NewGui.Instance.backgroundColor.getValue().getRGB());
            RenderUtil.drawOutlineRect(x, y, x + width, animHeight, NewGui.Instance.backgroundColor.getValue(), 1f);
        }
        {
            if (NewGui.Instance.icons.getValue())
                drawCategoryIcon();
        }
        {
            if (isOpened) {
                targetAnim = deltaY + height + 1;
                if (animHeight > targetAnim - NewGui.Instance.animationSpeed.getValue() && animHeight < targetAnim)
                    animHeight = targetAnim;
                else if (animHeight < targetAnim)
                    animHeight = AnimationUtil.increaseNumber(animHeight, targetAnim, NewGui.Instance.animationSpeed.getValue());
                else if (animHeight > targetAnim)
                    animHeight = AnimationUtil.decreaseNumber(animHeight, targetAnim, NewGui.Instance.animationSpeed.getValue());
            } else {
                targetAnim = y + height;
                animHeight = AnimationUtil.decreaseNumber(animHeight, targetAnim, NewGui.Instance.animationSpeed.getValue());
            }
            if (animHeight < y + height)
                animHeight = y + height;
            glPushMatrix();
            glPushAttrib(GL_SCISSOR_BIT);
            {
                RenderUtil.scissor(x, y + height, x + 1000, animHeight);
                glEnable(GL_SCISSOR_TEST);
            }
            newModuleArrayList.forEach(newModule -> newModule.drawScreen(mouseX, mouseY));
            glDisable(GL_SCISSOR_TEST);
            glPopAttrib();
            glPopMatrix();
        }
    }

    public void setScroll() {
        int dWheel = Mouse.getDWheel();
        for (Module module : Ruby.moduleManager.getModulesInCategory(category)) {
            if (dWheel < 0)
                module.scrollY -= NewGui.Instance.scrollSpeed.getValue();
            else if (dWheel > 0)
                module.scrollY += NewGui.Instance.scrollSpeed.getValue();
        }
    }

    public boolean isInsideFull(int mouseX, int mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y + height && mouseY < animHeight;
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (isOpened)
            newModuleArrayList.forEach(newModule -> newModule.keyTyped(typedChar, keyCode));

    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        {
            if (isInside(mouseX, mouseY)) {
                switch (mouseButton) {
                    case 0:
                        dragX = x - mouseX;
                        dragY = y - mouseY;
                        isDragging = true;
                        break;
                    case 1:
                        isOpened = !isOpened;
                        break;
                }
            }
        }
        {
            if (isOpened)
                newModuleArrayList.forEach(newModule -> newModule.mouseClicked(mouseX, mouseY, mouseButton));
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        {
            if (state == 0)
                isDragging = false;
        }
        {
            if (isOpened)
                newModuleArrayList.forEach(newModule -> newModule.mouseReleased(mouseX, mouseY, state));
        }
    }

    public boolean isInside(int mouseX, int mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public void drawCategoryIcon() {
        GlStateManager.enableAlpha();
        Ruby.mc.getTextureManager().bindTexture(new ResourceLocation("textures/icons/" + category.toString().toLowerCase() + ".png"));
        GlStateManager.color(1f, 1f, 1f);
        GL11.glPushMatrix();
        GuiScreen.drawScaledCustomSizeModalRect((int) (x + (width / 2f) - (Ruby.rubyFont.getStringWidth(category.toString()) / 2f) - 14), y + 1, 0, 0, 13, 12, 13, 12, 13, 12);
        GlStateManager.disableAlpha();
        GL11.glPopMatrix();
    }
}