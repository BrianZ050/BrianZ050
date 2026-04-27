const map = L.map("leaflet-map", {
	zoomControl: true,
	attributionControl: true,
});

const markerIcons = {
	access: "<svg viewBox='0 0 24 24' aria-hidden='true'><path d='M12 2l8 3v6c0 5.2-3.5 9.7-8 11.8C7.5 20.7 4 16.2 4 11V5l8-3z'></path></svg>",
	connect: "<svg viewBox='0 0 24 24' aria-hidden='true'><path d='M3 8.5C8.7 3.7 15.3 3.7 21 8.5'></path><path d='M6 12c3.5-3 8.5-3 12 0'></path><path d='M9.5 15.2c1.7-1.4 3.3-1.4 5 0'></path><circle cx='12' cy='18' r='1.2'></circle></svg>",
	utility: "<svg viewBox='0 0 24 24' aria-hidden='true'><path d='M9 3h2v5h2V3h2v5h1c1.1 0 2 .9 2 2v2c0 2.8-1.9 5.2-4.6 5.9V21h-4.8v-3.1C6.9 17.2 5 14.8 5 12v-2c0-1.1.9-2 2-2h1V3z'></path></svg>",
	commerce: "<svg viewBox='0 0 24 24' aria-hidden='true'><rect x='3' y='5' width='18' height='14' rx='2'></rect><path d='M5 9h14'></path><path d='M7 14h5'></path></svg>",
	route: "<svg viewBox='0 0 24 24' aria-hidden='true'><circle cx='12' cy='11' r='3'></circle><path d='M12 2v3'></path><path d='M12 19v3'></path><path d='M5.5 5.5l2.1 2.1'></path><path d='M18.5 5.5l-2.1 2.1'></path><path d='M5.5 18.5l2.1-2.1'></path><path d='M18.5 18.5l-2.1-2.1'></path></svg>",
};

const campusNodes = [
	{
		name: "College Ave Gate",
		category: "access",
		coords: [40.4997, -74.4499],
		description: "Badge checks, cameras, and pedestrian flow shape the first layer of the campus safety stack.",
		profile: "#college-ave-gate",
	},
	{
		name: "Parking Deck Pay Station",
		category: "access",
		coords: [40.5011, -74.4469],
		description: "Payment, camera, and plate data converge here, linking transit, money, and identity.",
		profile: "#parking-pay-station",
	},
	{
		name: "Alexander Library Wi-Fi Commons",
		category: "connect",
		coords: [40.4989, -74.4467],
		description: "Dense device traffic, shared study tables, and steady network demand make this a high-visibility node.",
		profile: "#library-wifi-commons",
	},
	{
		name: "Student Center Charging Bar",
		category: "utility",
		coords: [40.5019, -74.4466],
		description: "Power access is also trust access: the same outlets that recharge devices can expose them.",
		profile: "#student-center-charging-bar",
	},
	{
		name: "Residence Hall TV Lounge",
		category: "utility",
		coords: [40.4992, -74.4529],
		description: "Shared screens, remote controls, and group downtime form a social layer of low-friction exposure.",
		profile: "#residence-hall-tv-lounge",
	},
	{
		name: "Douglass NFC Kiosk",
		category: "commerce",
		coords: [40.4819, -74.4388],
		description: "Payment taps and food traffic make this a quiet but constant node in the trust network.",
		profile: "#douglass-nfc-kiosk",
	},
	{
		name: "Service Elevator",
		category: "route",
		coords: [40.5009, -74.4447],
		description: "Enclosed vertical circulation concentrates short interactions, access panels, and occasional surveillance; elevators can affect privacy and safety differently than open waiting areas.",
		profile: "#elevator",
	},
];

L.tileLayer("https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png", {
	maxZoom: 19,
	attribution: '&copy; <a href="https://carto.com/attributions">CARTO</a> &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
}).addTo(map);

map.setView([40.5004, -74.4488], 15);

const markers = campusNodes.map((node) => {
	const icon = L.divIcon({
		className: `map-marker marker-${node.category}`,
		html: `<div class="map-marker-shell marker-${node.category}"><span>${markerIcons[node.category]}</span></div>`,
		iconSize: [42, 42],
		iconAnchor: [21, 42],
		popupAnchor: [0, -36],
	});

	const marker = L.marker(node.coords, { icon }).addTo(map);
	marker.bindPopup(`
		<h3>${node.name}</h3>
		<p>${node.description}</p>
		<p><a href="${node.profile}">Open profile</a></p>
	`);
	return marker;
});

const campusGroup = L.featureGroup(markers);
map.fitBounds(campusGroup.getBounds().pad(0.22));
// Public-hotspot markers (no coverage/heatmap data available)
const publicHotspots = [
	{ name: "Alexander Library Wi‑Fi Commons", coords: [40.4989, -74.4467] },
	{ name: "Student Center (public Wi‑Fi nearby)", coords: [40.5019, -74.4466] },
	{ name: "Residence Hall TV Lounge (guest Wi‑Fi)", coords: [40.4992, -74.4529] },
];

const hotspotMarkers = L.layerGroup(
	publicHotspots.map((h) => {
		const icon = L.divIcon({
			className: `map-marker marker-connect`,
			html: `<div class="map-marker-shell marker-connect"><span>${markerIcons.connect}</span></div>`,
			iconSize: [42, 42],
			iconAnchor: [21, 42],
			popupAnchor: [0, -36],
		});
		return L.marker(h.coords, { icon }).bindPopup(`<strong>${h.name}</strong>`);
	})
);

// Only expose the hotspot layer in the overlay control
L.control.layers(null, { 'Public hotspots': hotspotMarkers }, { collapsed: false }).addTo(map);

// Add hotspots by default
hotspotMarkers.addTo(map);

const legend = L.control({ position: "bottomright" });

legend.onAdd = function () {
	const container = L.DomUtil.create("div", "leaflet-control leaflet-bar map-legend-control");
	container.innerHTML = `
		<div class="map-legend-title">Legend</div>
		<div class="map-legend-item"><span class="legend-dot legend-access"></span> Access control</div>
		<div class="map-legend-item"><span class="legend-dot legend-connect"></span> Connectivity</div>
		<div class="map-legend-item"><span class="legend-dot legend-utility"></span> Utility and media</div>
		<div class="map-legend-item"><span class="legend-dot legend-commerce"></span> Commerce</div>
		<div class="map-legend-item"><span class="legend-dot legend-route"></span> Night route</div>
	`;
	L.DomEvent.disableClickPropagation(container);
	L.DomEvent.disableScrollPropagation(container);
	return container;
};

legend.addTo(map);