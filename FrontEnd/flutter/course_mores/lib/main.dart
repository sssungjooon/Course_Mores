import 'package:coursemores/getx_controller.dart';
import 'package:flashy_tab_bar2/flashy_tab_bar2.dart';
import 'package:flutter/material.dart' hide ModalBottomSheetRoute;
import 'package:kakao_flutter_sdk/kakao_flutter_sdk.dart';
import 'style.dart' as style;
import 'notification/notification.dart' as noti;
import 'course_search/search.dart' as search;
import 'home_screen.dart' as home;
import 'mypage.dart' as mypage;
import 'course_make/make_start.dart' as make;
import 'login_page.dart' as login;
import 'package:get/get.dart';

final loginController = Get.put(LoginStatus());
final pageController = Get.put(PageNum());
var pageNum = pageController.pageNum.value;

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  KakaoSdk.init(nativeAppKey: '59816c34bd5a0094d4f29bf08b55a34c');
  runApp(GetMaterialApp(theme: style.theme, home: const MyApp()));
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) {
    return Obx(() {
      print(pageNum);
      if (loginController.isLoggedIn.value == true) {
        return Scaffold(
            backgroundColor: const Color.fromARGB(255, 240, 240, 240),
            appBar: const CustomAppBar(),
            body: [
              const home.HomeScreen(),
              const make.MakeStart(),
              const search.Search(),
              const Text("관심페이지"),
              const mypage.MyPage(),
            ][pageNum],
            bottomNavigationBar: FlashyTabBar(
              selectedIndex: pageNum,
              showElevation: true,
              onItemSelected: (index) => setState(() {
                pageNum = index;
              }),
              items: [
                FlashyTabBarItem(
                  icon: const Icon(Icons.home),
                  title: const Text('홈'),
                ),
                FlashyTabBarItem(
                  icon: const Icon(Icons.route),
                  title: const Text('코스'),
                ),
                FlashyTabBarItem(
                  icon: const Icon(Icons.search),
                  title: const Text('검색'),
                ),
                FlashyTabBarItem(
                  icon: const Icon(Icons.bookmark),
                  title: const Text('관심'),
                ),
                FlashyTabBarItem(
                  icon: const Icon(Icons.person),
                  title: const Text('마이페이지'),
                ),
              ],
            ));
      } else {
        return login.LoginPage();
      }
    });
  }
}

class CustomAppBar extends StatelessWidget implements PreferredSizeWidget {
  const CustomAppBar({
    super.key,
  });

  @override
  Widget build(BuildContext context) {
    return AppBar(
      title: const Text('CourseMores', style: TextStyle(color: Colors.black)),
      centerTitle: true,
      actions: [
        IconButton(
          icon: const Icon(
            Icons.notifications,
            color: Colors.black,
          ),
          onPressed: () {
            Navigator.push(
              context,
              MaterialPageRoute(
                  builder: (context) => const noti.Notification()),
            );
          },
        )
      ],
    );
  }

  @override
  Size get preferredSize => const Size.fromHeight(kToolbarHeight);
}