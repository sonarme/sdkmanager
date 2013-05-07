'use strict';

/* Controllers */

angular.module('dashboard.controllers', [])
    .controller('Analytics', ['$scope', function ($scope) {

    }])
    .controller('MarketingBuild', ['$scope', 'Campaign', 'Geofence', function ($scope, Campaign, Geofence) {
        $scope.campaign = {};
        $scope.predicate = 'and';
        $scope.attributes = [

            {name: 'Gender', id: 'gender', group: 'Attribute', type: 'c', options: ['male', 'female']},
            {name: 'Income', id: 'income', group: 'Attribute', type: 'c', options: ['50k', '100k']}

        ];
        $scope.dows = [
            {name: 'Monday', id: 2},
            {name: 'Tuesday', id: 3},
            {name: 'Wednesday', id: 4},
            {name: 'Thursday', id: 5},
            {name: 'Friday', id: 6},
            {name: 'Saturday', id: 7},
            {name: 'Sunday', id: 1}
        ];
        var hourMs = 60 * 60 * 1000
        $scope.frequencyCaps = [
            {name: 'per hour', id: hourMs},
            {name: 'per day', id: 24 * hourMs},
            {name: 'per week', id: 7 * 24 * hourMs},
            {name: 'per month', id: 30 * 7 * 24 * hourMs},
            {name: 'ever', id: -1}
        ];
        $scope.frequencyCap = $scope.frequencyCaps[$scope.frequencyCaps.length - 1];
        $scope.compareOps = {
            'c': [
                {name: 'equals', id: 'EQUAL'},
                {name: 'does not equal', id: 'NOTEQUAL'}
            ],
            'n': [
                {name: 'equals', id: 'EQUAL'},
                {name: 'does not equal', id: 'NOTEQUAL'},
                {name: 'less than', id: 'LESS'},
                {name: 'less than or equal', id: 'LESSOREQUAL'},
                {name: 'greater than', id: 'GREATER'},
                {name: 'greater than or equal', id: 'GREATEROREQUAL'}
            ]
        }
        $scope.geofenceActions = [
            {name: "Arriving at", id: true},
            {name: "Leaving", id: false}
        ]
        $scope.dateTimeOptions = [
            {name: "ASAP", id: true},
            {name: "Specific date & time", id: false}
        ]
        $scope.asap = $scope.dateTimeOptions[0];
        Geofence.query(null, function (data) {
            $scope.geofenceLists = data;
            for (var i = 0; i < data.length; ++i) {
                $scope.attributes.push(
                    {name: "Visits of " + data[i].name, id: data[i].id, group: 'Visits', type: 'n'}
                )
            }
        }, function (data) {
        })
        $scope.clauses = [

        ];
        $scope.geofenceEntries = [
        ];
        $scope.dowSelected = {
        };
        function _fn_error(err) {
            alert(err);

        }

        function _fn_success_put_post(data) {
        }

        $scope.setPredicate = function (pred) {
            $scope.predicate = pred;
        };
        function idMap(el) {
            return el.id;
        }

        $scope.save = function () {
            var campaign = new Campaign($scope.campaign);
            campaign.clauses = $scope.clauses.map(idMap);
            campaign.attributes = $scope.attributes.map(idMap);
            campaign.geofenceEntries = $scope.geofenceEntries;
            campaign.dowSelected = $scope.dowSelected;
            campaign.frequencyCap = $scope.frequencyCap;
            campaign.$save();
        };
        $scope.removeClause = function (clause) {
            $scope.clauses.splice($scope.clauses.indexOf(clause), 1);
        }
        $scope.createClause = function () {
            $scope.clauses.push(
                {attribute: $scope.attributes[0],
                    compareOp: $scope.compareOps[$scope.attributes[0].type][0],
                    values: {},
                    value: $scope.attributes[0].options[0]
                });
        }
        $scope.isDowSelected = function () {
            for (var dow in $scope.dowSelected) {
                if ($scope.dowSelected[dow]) return true;
            }
            return false;
        }
        $scope.removeGeofenceEntry = function (ga) {
            $scope.geofenceEntries.splice($scope.geofenceEntries.indexOf(ga), 1);
        }
        $scope.addGeofenceEntry = function () {
            $scope.geofenceEntries.push({
                geofenceAction: $scope.geofenceActions[0],
                geofenceList: $scope.geofenceLists[0]
            });
        }
        $scope.refreshDowAll = function () {
            for (var i = 1; i <= 7; ++i)
                $scope.dowSelected[i] = $scope.dowAll;
        }
        $scope.allDowSelected = function () {
            for (var i = 1; i <= 7; ++i)
                if (!$scope.dowSelected[i]) return false;
            return true;
        }
        $scope.attributeText = function (values) {
            var value = "";
            for (var k in values) {
                if (values[k]) value += value === "" ? k : ", " + k;
            }
            return value === "" ? 'None' : value;
        }
        $scope.remainingAttributes = function (currentAttribute) {
            var used = {};
            for (var i = 0; i < $scope.clauses.length; ++i) {
                used[$scope.clauses[i].attribute.id] = true;
            }
            return $scope.attributes.filter(function (obj) {
                return obj.id === currentAttribute.id || !used[obj.id];
            })
        }
    }])
    .controller('GeofenceBuildCtrl', ['$scope', 'Factual', function ($scope, Factual) {
        $scope.searchedPlaces = [];
        $scope.placesAdded = [];
        $scope.bounds = new google.maps.LatLngBounds();

        $scope.mapOptions = {
            center: new google.maps.LatLng(40.745394, -73.9870),
            zoom: 15,
            mapTypeId: google.maps.MapTypeId.ROADMAP
        };

        function addMarker(place, selected) {
            var marker = new google.maps.Marker({
                map: $scope.myMap,
                icon: "http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|" + (selected ? "4169E1" : "808080"),
                position: new google.maps.LatLng(place.latitude, place.longitude)
            });
            $scope.bounds.extend(new google.maps.LatLng(place.latitude, place.longitude));
            place.marker = marker;
            google.maps.event.addListener(marker, 'click', function () {
                $scope.currentMarker = marker;
                $scope.currentPlace = place;
                $scope.myInfoWindow.open($scope.myMap, marker);
            });
        }

        function resetMap() {
            var place;
            while ($scope.searchedPlaces.length) {
                place = $scope.searchedPlaces.pop();
                place.marker.setMap(null);
            }
            $scope.bounds = new google.maps.LatLngBounds(); //reset the bounds
            for (var i = 0; i < $scope.placesAdded.length; i++) {
                place = $scope.placesAdded[i];
                $scope.bounds.extend(new google.maps.LatLng(place.latitude, place.longitude));
            }

        }

        function arrayContainsPlace(array, place) {
            var index = array.map(function (p) {
                return p.factual_id;
            }).indexOf(place.factual_id);
            return index > -1;
        }

        $scope.search = function () {
            resetMap();
            $scope.factual.limit = 20;
            Factual.get($scope.factual, function (data) {
                $scope.placesData = data.places;
                var places = data.places.data;
                var facets = data.facets.data;
                for (var key in facets) {
                    $scope['typeahead_' + key] = Object.keys(facets[key]).sort();
                }
                var place;
                for (var i = 0; i < places.length; i++) {
                    place = places[i];
                    if ((place.latitude !== undefined || place.longitude !== undefined)) {
                        addMarker(place, arrayContainsPlace($scope.placesAdded, place));
                        $scope.searchedPlaces.push(place);
                    }
                }
                $scope.myMap.fitBounds($scope.bounds);
            }, function (error) {
                alert(error);
            });
        }

        $scope.showResultsTable = function () {
            return $scope.placesData !== undefined;
        }

        $scope.panToPlace = function (place) {
            $scope.myMap.panTo(new google.maps.LatLng(place.latitude, place.longitude));
            google.maps.event.trigger(place.marker, 'click');
        }

        $scope.removePlace = function (index) {
            var place = $scope.searchedPlaces.splice(index, 1);
            place[0].marker.setMap(null);
        }

        $scope.addToList = function () {
            var place;
            while ($scope.searchedPlaces.length) {
                place = $scope.searchedPlaces.pop();
                if (!arrayContainsPlace($scope.placesAdded, place)) {
                    place.marker.setIcon("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|4169E1");
                    $scope.placesAdded.push(place)
                }
            }
        }

        $scope.clearList = function () {
            while ($scope.placesAdded.length) {
                var place = $scope.placesAdded.pop();
                var placeIndex = $scope.searchedPlaces.map(function (p) {
                    return p.factual_id;
                }).indexOf(place.factual_id);
                if (placeIndex > -1)
                    $scope.searchedPlaces[placeIndex].marker.setIcon("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|808080");
            }
        }

        $scope.saveList = function (name) {
            alert("saved: " + name);
        }
    }]);