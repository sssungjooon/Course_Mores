import 'package:flutter/material.dart';
import 'package:google_maps_webservice/places.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';

class CMSearch extends StatefulWidget {
  const CMSearch({super.key});

  @override
  State<CMSearch> createState() => _CMSearchState();
}

class _CMSearchState extends State<CMSearch> {
  final _placesApiClient =
      GoogleMapsPlaces(apiKey: 'AIzaSyCEfaZrsmBfK6PeG5vBBXwSZ09doSPuXCE');
  final _searchController = TextEditingController();
  List<PlacesSearchResult> _searchResults = [];

  LatLng? _selectedLocation;

  void _onSearchPressed() async {
    final response = await _placesApiClient.searchByText(_searchController.text,
        language: 'ko');
    if (response.isOkay) {
      setState(() {
        _searchResults = response.results;
      });
    }
  }

  void _onSearchChanged(String value) async {
    final response = await _placesApiClient.searchByText(value, language: 'ko');
    if (response.isOkay) {
      setState(() {
        _searchResults = response.results;
      });
    }
  }

  void _onPlaceSelected(PlacesSearchResult place) {
    // 선택된 장소 정보를 이용하여 페이지 이동 등의 로직을 작성합니다.
    setState(() {
      _selectedLocation = LatLng(
        place.geometry!.location.lat,
        place.geometry!.location.lng,
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        // 없어도 <- 모양의 뒤로가기가 기본으로 있으나 < 모양으로 바꾸려고 추가함
        leading: IconButton(
          icon: const Icon(
            Icons.navigate_before,
            color: Colors.black,
          ),
          onPressed: () {
            Navigator.pop(context);
          },
        ),
        // 알림 아이콘과 텍스트 같이 넣으려고 RichText 사용
        title: RichText(
            text: const TextSpan(
          children: [
            WidgetSpan(
              child: Icon(
                Icons.edit_note,
                color: Colors.black,
              ),
            ),
            WidgetSpan(
              child: SizedBox(
                width: 5,
              ),
            ),
            TextSpan(
              text: '장소 검색으로 추가하기',
              style: TextStyle(
                fontSize: 22,
                color: Colors.black,
              ),
            ),
          ],
        )),
        // 피그마와 모양 맞추려고 close 아이콘 하나 넣어둠
        // <와 X 중 하나만 있어도 될 것 같아서 상의 후 삭제 필요
        actions: [
          IconButton(
              onPressed: () {
                Navigator.pop(context);
              },
              icon: const Icon(
                Icons.close,
                color: Colors.black,
              )),
        ],
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _searchController,
                    decoration: InputDecoration(
                      hintText: '장소 검색',
                    ),
                    onChanged: _onSearchChanged,
                  ),
                ),
                const SizedBox(width: 8),
                ElevatedButton(
                  onPressed: _onSearchPressed,
                  child: Text('검색'),
                ),
              ],
            ),
          ),
          Expanded(
            child: ListView.builder(
              itemCount: _searchResults.length,
              itemBuilder: (context, index) {
                final place = _searchResults[index];
                return ListTile(
                  title: Text(place.name),
                  subtitle: Text(place.formattedAddress ?? ''),
                  onTap: () => _onPlaceSelected(place),
                );
              },
            ),
          ),
          if (_selectedLocation != null)
            Text(
                '선택된 장소: ${_selectedLocation!.latitude}, ${_selectedLocation!.longitude}')
        ],
      ),
    );
  }
}