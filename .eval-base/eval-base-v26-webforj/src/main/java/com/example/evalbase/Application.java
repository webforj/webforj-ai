package com.example.evalbase;

import com.webforj.App;
import com.webforj.annotation.AppProfile;
import com.webforj.annotation.AppTheme;
import com.webforj.annotation.Routify;
import com.webforj.annotation.StyleSheet;

@Routify(packages = "com.example.evalbase.views")
@StyleSheet("ws://app.css")
@AppTheme("system")
@AppProfile(name = "eval-base-v26-webforj", shortName = "eval-base-v26-webforj")
public class Application extends App {
}
