//{"name": string, "imagelink": imagelink, "ingredient": string list, "recipe": string list, "recipe_imagelink": imagelink lisg, "youtubelink": link list}
package com.example.frontapp;

public class Recipe {
    private String cook_name;
    private String cook_image;
    private String [] ingredients;
    private String [] recipes;
    private String [] recipe_images;
    private String [] recipe_videos;

    public String getCookName() {
        return cook_name;
    }
    public String getCookImage() {
        return cook_image;
    }
    public String [] getIngredients() {
        return ingredients;
    }
    public String [] getRecipes() {
        return recipes;
    }
    public String [] getRecipeImages() {
        return recipe_images;
    }
    public String [] getRecipeVideos() {
        return recipe_videos;
    }

}
