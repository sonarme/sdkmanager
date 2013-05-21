'use strict';

/* Controllers */

angular.module('dashboard.controllers', [])
    .controller('AnalyticsCtrl', ['$scope', 'Analytics', 'GeofenceList', function ($scope, Analytics, GeofenceList) {
        $scope.geofenceListId = "walmart"
        $scope.appId = "test"

        $scope.filters = {
            what_action: [
                {name: 'Targeted by', id: 'targeted_by'},
                {name: 'Received message', id: 'received_message'}
            ],
            what_campaign: [
                {name: 'Like BigMac Prompt', id: 'like_bigmac_prompt'},
                {name: 'Redeemed Coupon', id: 'redeemed_coupon'}
            ],
            where: [
                {name: 'All Places', id: 'all_places'}
            ],
            when: [
                {name: 'Past 96 Hours', id: 345600000},
                {name: 'Past 30 Days', id: 2592000000},
                {name: 'Past 24 Weeks', id: 14515200000},
                {name: 'Past 12 Months', id: 31536000000}/*,
                 {name: 'Pick a Date Range...', id: 'pick_a_date_range'}*/
            ]
        }

        $scope.placeFilters = {
            measures: [
                {name: 'Visits', id: 'visits'},
                {name: 'Dwell time', id: 'dwelltime'}
            ],
            aggregates: [
                {name: 'Unique', id: 'unique'},
                {name: 'Total', id: 'total'},
                {name: 'Average', id: 'average'}
            ], /*
             displays: [
             {name: 'Pie', id: 'pie'},
             {name: 'Line', id: 'line'},
             {name: 'Map', id: 'map'}
             ],*/
            times: [
                {name: 'Time of Day', id: 'timeOfDay'},
                {name: 'Hour', id: 'hour'},
                {name: 'Day', id: 'day'},
                {name: 'Week', id: 'week'},
                {name: 'Month', id: 'month'}
            ]
        }
        $scope.customerFilters = {
            measures: [
                {name: 'Gender', id: 'gender'},
                {name: 'Age', id: 'age'},
                {name: 'Income', id: 'income'},
                {name: 'Education Level', id: 'education'},
                {name: 'Ethnicity', id: 'ethnicity'},
                {name: 'Household', id: 'household'}
            ]
        }

        $scope.messageFilters = {
            measures: [
                {name: 'Messages Sent', id: 'messages_sent'},
                {name: 'Messages Opened', id: 'messages_opened'}
            ],
            aggregates: [
                {name: 'Unique', id: 'unique'},
                {name: 'Total', id: 'total'},
                {name: 'Average', id: 'average'}
            ],
            displays: [
                {name: 'Pie', id: 'pie'},
                {name: 'Line', id: 'line'},
                {name: 'Map', id: 'map'}
            ],
            times: [
                {name: 'Time of Day', id: 'timeOfDay'},
                {name: 'Hour', id: 'hour'},
                {name: 'Day', id: 'day'},
                {name: 'Week', id: 'week'},
                {name: 'Month', id: 'month'}
            ]
        }

        $scope.current = {
            filters: {
                what_action: $scope.filters.what_action[0],
                what_campaign: $scope.filters.what_campaign[0],
                who: {},
                where: $scope.filters.where[0],
                when: $scope.filters.when[0]
            },
            places: {
                measure: $scope.placeFilters.measures[0],
                aggregate: $scope.placeFilters.aggregates[0],
//                display: $scope.placeFilters.displays[0],
                time: $scope.placeFilters.times[0]
            },
            customers: {
                measure: $scope.customerFilters.measures[0]
            },
            messages: {
                measure: $scope.messageFilters.measures[0],
                aggregate: $scope.messageFilters.aggregates[0],
                display: $scope.messageFilters.displays[0],
                time: $scope.messageFilters.times[0]
            }
        }

        var resultsA = {
            facets: {
                Times: {
                    _type: "date_histogram",
                    entries: [
                        {
                            time: 1341100800000,
                            count: 9
                        },
                        {
                            time: 1343779200000,
                            count: 32
                        },
                        {
                            time: 1346457600000,
                            count: 78
                        },
                        {
                            time: 1349049600000,
                            count: 45
                        },
                        {
                            time: 1351728000000,
                            count: 134
                        }
                    ]
                }
            }
        };


        $scope.topPlaces = {}


        $scope.topPeople = {}

        $scope.changeFilter = function (fType, filter) {
            $scope.current.filters[fType] = filter
            refreshAnalytics()
        }

        $scope.changeAnalyticsFilter = function (aType, attribute, filter) {
            $scope.current[aType][attribute] = filter;
            getAnalytics(aType);
        }

        $scope.results = resultsA;

        function getAnalytics(aType) {
            Analytics.get({aType: aType,
                    type: $scope.current[aType] ? ($scope.current[aType].measure ? $scope.current[aType].measure.id : "") : "",
                    agg: $scope.current[aType] ? ($scope.current[aType].aggregate ? $scope.current[aType].aggregate.id : "") : "",
                    group: $scope.current[aType] ? ($scope.current[aType].time ? $scope.current[aType].time.id : "") : "",
                    geofenceListId: $scope.current.filters.where.id,
                    since: (new Date(2013, 2, 11, 0, 0, 0, 0).getTime() - $scope.current.filters.when.id),
                    appId: $scope.appId}, function (data) {
                    $scope[aType] = data;
                },
                function (err) {
                    alert("error");
                })
        }

        function init() {
            GeofenceList.get({appId: $scope.appId}, function (data) {
                if (data && data.list) {
                    var list = data.list;
                    for (var i = 0; i < list.length; ++i) {
                        $scope.filters.where.push(
                            {name: list[i].name, id: list[i].id}
                        )
                    }
                }
            })
            refreshAnalytics()
        }
        function refreshAnalytics() {
            getAnalytics('places')
            getAnalytics('customers')
            getAnalytics('topPlaces')
            getAnalytics('topPeople')
        }

        init()

    }])
    .controller('CampaignsCtrl', ['$scope', 'Campaign', function ($scope, Campaign) {
        Campaign.query(null, function (data) {
            $scope.campaigns = data;
        }, function (data) {
        })
    }      ])
    .controller('MarketingBuild', ['$scope', 'Campaign', 'GeofenceList', function ($scope, Campaign, GeofenceList) {
        $scope.campaign = {};
        $scope.predicate = 'and';
        $scope.attributes = {

            gender: {name: 'Gender', group: 'Attribute', type: 'c', options: ['male', 'female']},
            income: {name: 'Income', group: 'Attribute', type: 'c', options: ['50k', '100k']}

        };
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
        $scope.frequencyCap = -1;
        $scope.compareOpsMap = {
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
        $scope.asap = true;

        GeofenceList.get({appId: "test"}, function (data) {
            $scope.geofenceLists = data.list;
            for (var i = 0; i < data.length; ++i) {
                $scope.attributes.push(
                    {name: "Visits of " + data[i].name, id: data[i].id, group: 'Visits', type: 'n'}
                )
            }
        })

        $scope.dowAll = false;
        $scope.clauses = [

        ];
        $scope.geofenceEntries = [
        ];
        $scope.dowSelected = {
        };

        $scope.setPredicate = function (pred) {
            $scope.predicate = pred;
        };
        $scope.save = function () {
            var campaign = new Campaign($scope.campaign);
            campaign.clauses = $scope.clauses;
            campaign.geofenceEntries = $scope.geofenceEntries;
            campaign.dowSelected = Object.keys($scope.dowSelected).map(parseInt);
            campaign.frequencyCap = $scope.frequencyCap.id;
            alert(JSON.stringify(campaign, null, 2));
            campaign.$save();
        };
        $scope.removeClause = function (clause) {
            $scope.clauses.splice($scope.clauses.indexOf(clause), 1);
        }
        $scope.attributeType = function (attribute) {
            var fromMap = $scope.attributes[attribute];
            return fromMap ? fromMap.type : 'n';
        }
        $scope.compareOps = function (attribute) {
            var ops = $scope.compareOpsMap[$scope.attributeType(attribute)];
            return ops;
        }
        $scope.numericType = function (attribute) {
            return $scope.attributeType(attribute) === 'n';
        }
        $scope.changeAttribute = function (clause) {
            clause.value = '';
            clause.compareOp = 'EQUAL';
        }
        $scope.createClause = function () {
            $scope.clauses.push(
                {attribute: 'gender',
                    compareOp: 'EQUAL',
                    values: {},
                    value: ''
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
                entering: true,
                geofenceListId: $scope.geofenceLists[0].id
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
    }])
    .controller('GeofenceBuildCtrl', ['$scope', 'Factual', 'Geofence', function ($scope, Factual, Geofence) {
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
            var geofence = new Geofence();
            geofence.name = name;
            geofence.appId = "testApp"; //todo: change this
            geofence.places = []
            var place;
            for (var i = 0; i < $scope.placesAdded.length; i++) {
                place = $scope.placesAdded[i];
                geofence.places.push({id: "factual-" + place.factual_id, name: place.name, lat: place.latitude, lng: place.longitude, type: "factual"})
            }
            geofence.$save(function (data) {
                console.log(data);
                alert("Saved " + name);
            })
        }
    }])
    .controller('GeofenceListsCtrl', ['$scope', 'GeofenceList', function ($scope, GeofenceList) {
        $scope.geofenceLists = GeofenceList.get({appId: "test"})
    }]);