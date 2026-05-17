import { useState, useEffect } from 'react';
import TopBar from '../../components/common/TopBar';
import Modal from '../../components/common/Modal';
import DataTable from '../../components/common/DataTable';
import {
  getResourceTypes, createResourceType, deleteResourceType,
  getResources, createResource, deleteResource,
  getTimeSlotsByResource, createTimeSlot,
  getSeatsByResource, createSeat,
  getQuotasByResource, createQuota,
} from '../../api/resourceApi';
import { Plus, Trash2, ChevronRight } from 'lucide-react';
import { typeLabel, typeColor, typeBg, typeIcon } from '../../utils/formatters';
import toast from 'react-hot-toast';

const TYPES = ['TIME_BASED', 'RESOURCE_BASED', 'SEAT_BASED', 'QUOTA_BASED', 'CAPACITY_BASED'];

export default function ResourcesPage() {
  const [tab, setTab] = useState('types');
  const [resourceTypes, setResourceTypes] = useState([]);
  const [resources, setResources] = useState([]);
  const [selectedResource, setSelectedResource] = useState(null);
  const [subData, setSubData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modal, setModal] = useState(null); // 'type' | 'resource' | 'slot' | 'seat' | 'quota'

  // Forms
  const [rtForm, setRtForm] = useState({ name: '', description: '', reservationType: 'TIME_BASED' });
  const [rForm, setRForm] = useState({ name: '', description: '', resourceTypeId: '', totalCapacity: '' });
  const [slotForm, setSlotForm] = useState({ resourceId: '', startTime: '', endTime: '', maxParallelCapacity: 1 });
  const [seatForm, setSeatForm] = useState({ resourceId: '', seatIdentifier: '', seatRow: '', seatColumn: '' });
  const [quotaForm, setQuotaForm] = useState({ resourceId: '', quotaName: '', maxAllocation: '' });

  const loadAll = async () => {
    setLoading(true);
    try {
      const [rt, r] = await Promise.all([getResourceTypes(), getResources()]);
      setResourceTypes(rt.data?.data ?? rt.data ?? []);
      setResources(r.data?.data ?? r.data ?? []);
    } catch (e) { toast.error(e.message); }
    finally { setLoading(false); }
  };

  const loadSubData = async (resource) => {
    setSelectedResource(resource);
    try {
      const type = resource.reservationType;
      if (type === 'TIME_BASED') {
        const r = await getTimeSlotsByResource(resource.id);
        setSubData(r.data?.data ?? r.data ?? []);
      } else if (type === 'SEAT_BASED') {
        const r = await getSeatsByResource(resource.id);
        setSubData(r.data?.data ?? r.data ?? []);
      } else if (type === 'QUOTA_BASED') {
        const r = await getQuotasByResource(resource.id);
        setSubData(r.data?.data ?? r.data ?? []);
      } else { setSubData([]); }
    } catch { setSubData([]); }
  };

  useEffect(() => { loadAll(); }, []);

  const submitResourceType = async (e) => {
    e.preventDefault();
    try {
      await createResourceType(rtForm);
      toast.success('Resource type created');
      setModal(null);
      setRtForm({ name: '', description: '', reservationType: 'TIME_BASED' });
      loadAll();
    } catch (e) { toast.error(e.message); }
  };

  const submitResource = async (e) => {
    e.preventDefault();
    try {
      await createResource({ ...rForm, resourceTypeId: Number(rForm.resourceTypeId), totalCapacity: rForm.totalCapacity ? Number(rForm.totalCapacity) : null });
      toast.success('Resource created');
      setModal(null);
      setRForm({ name: '', description: '', resourceTypeId: '', totalCapacity: '' });
      loadAll();
    } catch (e) { toast.error(e.message); }
  };

  const submitSlot = async (e) => {
    e.preventDefault();
    try {
      await createTimeSlot({ ...slotForm, resourceId: selectedResource.id, maxParallelCapacity: Number(slotForm.maxParallelCapacity) });
      toast.success('Time slot created');
      setModal(null);
      loadSubData(selectedResource);
    } catch (e) { toast.error(e.message); }
  };

  const submitSeat = async (e) => {
    e.preventDefault();
    try {
      await createSeat({ ...seatForm, resourceId: selectedResource.id, seatColumn: Number(seatForm.seatColumn) });
      toast.success('Seat created');
      setModal(null);
      loadSubData(selectedResource);
    } catch (e) { toast.error(e.message); }
  };

  const submitQuota = async (e) => {
    e.preventDefault();
    try {
      await createQuota({ ...quotaForm, resourceId: selectedResource.id, maxAllocation: Number(quotaForm.maxAllocation) });
      toast.success('Quota created');
      setModal(null);
      loadSubData(selectedResource);
    } catch (e) { toast.error(e.message); }
  };

  const handleDeleteRT = async (id) => {
    if (!confirm('Delete this resource type?')) return;
    try { await deleteResourceType(id); toast.success('Deleted'); loadAll(); }
    catch (e) { toast.error(e.message); }
  };

  const handleDeleteR = async (id) => {
    if (!confirm('Delete this resource?')) return;
    try { await deleteResource(id); toast.success('Deleted'); if (selectedResource?.id === id) setSelectedResource(null); loadAll(); }
    catch (e) { toast.error(e.message); }
  };

  const resType = selectedResource?.reservationType;

  return (
    <div className="page-wrapper">
      <TopBar
        title="Resources"
        subtitle="Manage resource types, resources, and their configurations"
        actions={
          <div className="flex gap-2">
            <button className="btn btn-secondary btn-sm" onClick={() => setModal('type')}>
              <Plus size={14} /> Resource Type
            </button>
            <button className="btn btn-primary btn-sm" onClick={() => setModal('resource')}>
              <Plus size={14} /> Resource
            </button>
          </div>
        }
      />

      <div className="app-content animate-fade-in">
        {/* Tabs */}
        <div className="tabs" style={{ maxWidth: 320, marginBottom: 'var(--space-5)' }}>
          <button className={`tab-btn${tab === 'types' ? ' active' : ''}`} onClick={() => setTab('types')}>Resource Types</button>
          <button className={`tab-btn${tab === 'resources' ? ' active' : ''}`} onClick={() => setTab('resources')}>Resources</button>
        </div>

        {tab === 'types' && (
          <div className="card">
            <DataTable
              loading={loading}
              columns={[
                { key: 'id', label: 'ID', width: 60 },
                { key: 'name', label: 'Name', render: (v) => <strong>{v}</strong> },
                { key: 'reservationType', label: 'Type', render: (v) => (
                  <span className="badge" style={{ background: typeBg(v), color: typeColor(v) }}>
                    {typeIcon(v)} {typeLabel(v)}
                  </span>
                )},
                { key: 'description', label: 'Description' },
                { key: 'id', label: 'Actions', render: (id) => (
                  <button className="btn btn-ghost btn-icon btn-sm" onClick={() => handleDeleteRT(id)} style={{ color: 'var(--color-danger)' }}>
                    <Trash2 size={14} />
                  </button>
                )},
              ]}
              data={resourceTypes}
              emptyMessage="No resource types yet. Create one to get started."
            />
          </div>
        )}

        {tab === 'resources' && (
          <div className="resources-layout">
            {/* Resource list */}
            <div className="card card--flat" style={{ padding: 0, overflow: 'hidden' }}>
              <div style={{ padding: 'var(--space-4) var(--space-5)', borderBottom: '1px solid var(--color-border-light)' }}>
                <div className="card__title">All Resources</div>
              </div>
              {loading ? (
                <div className="loading-screen" style={{ height: 120 }}><div className="spinner" /></div>
              ) : resources.length === 0 ? (
                <div className="empty-state" style={{ padding: 'var(--space-8)' }}>
                  <div className="empty-state__title">No resources yet</div>
                </div>
              ) : (
                resources.map((res) => (
                  <div
                    key={res.id}
                    onClick={() => loadSubData(res)}
                    style={{
                      padding: 'var(--space-4) var(--space-5)',
                      borderBottom: '1px solid var(--color-border-light)',
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      background: selectedResource?.id === res.id ? 'var(--color-accent-light)' : 'transparent',
                      transition: 'background 0.15s',
                    }}
                  >
                    <div>
                      <div style={{ fontWeight: 600, fontSize: 'var(--font-size-sm)' }}>{res.name}</div>
                      <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--color-text-secondary)', marginTop: 2 }}>
                        {typeIcon(res.reservationType)} {typeLabel(res.reservationType)}
                        {res.totalCapacity ? ` · Cap: ${res.totalCapacity}` : ''}
                      </div>
                    </div>
                    <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                      <span className={`badge ${res.isActive ? 'badge-active' : 'badge-inactive'}`}
                        style={{ fontSize: '10px' }}>
                        {res.isActive ? 'Active' : 'Inactive'}
                      </span>
                      <button className="btn btn-ghost btn-icon btn-sm" onClick={(e) => { e.stopPropagation(); handleDeleteR(res.id); }} style={{ color: 'var(--color-danger)' }}>
                        <Trash2 size={13} />
                      </button>
                      <ChevronRight size={14} style={{ color: 'var(--color-text-muted)' }} />
                    </div>
                  </div>
                ))
              )}
            </div>

            {/* Sub-resources panel */}
            <div className="card">
              {!selectedResource ? (
                <div className="empty-state">
                  <div className="empty-state__icon"><ChevronRight size={22} /></div>
                  <div className="empty-state__title">Select a resource</div>
                  <div className="empty-state__desc">Click a resource from the list to view and manage its configuration</div>
                </div>
              ) : (
                <>
                  <div className="card__header">
                    <div>
                      <div className="card__title">{selectedResource.name}</div>
                      <div className="card__subtitle">
                        {typeIcon(resType)} {typeLabel(resType)} {selectedResource.totalCapacity ? `· Capacity: ${selectedResource.totalCapacity}` : ''}
                      </div>
                    </div>
                    {resType === 'TIME_BASED' && <button className="btn btn-primary btn-sm" onClick={() => setModal('slot')}><Plus size={14} /> Add Slot</button>}
                    {resType === 'SEAT_BASED' && <button className="btn btn-primary btn-sm" onClick={() => setModal('seat')}><Plus size={14} /> Add Seat</button>}
                    {resType === 'QUOTA_BASED' && <button className="btn btn-primary btn-sm" onClick={() => setModal('quota')}><Plus size={14} /> Add Quota</button>}
                  </div>
                  {subData.length === 0 ? (
                    <div className="empty-state">
                      <div className="empty-state__desc">
                        {resType === 'TIME_BASED' && 'No time slots defined. Add slots to allow bookings.'}
                        {resType === 'SEAT_BASED' && 'No seats defined. Add seats to enable seat selection.'}
                        {resType === 'QUOTA_BASED' && 'No quota pools defined. Add quota categories.'}
                        {(resType === 'RESOURCE_BASED' || resType === 'CAPACITY_BASED') && 'This resource type uses capacity/date-range based booking. No sub-entities needed.'}
                      </div>
                    </div>
                  ) : (
                    <div className="table-wrap">
                      <table className="table">
                        <thead>
                          <tr>
                            {resType === 'TIME_BASED' && <><th>ID</th><th>Start</th><th>End</th><th>Max Capacity</th><th>Current</th></>}
                            {resType === 'SEAT_BASED' && <><th>ID</th><th>Seat ID</th><th>Row</th><th>Col</th><th>Available</th></>}
                            {resType === 'QUOTA_BASED' && <><th>ID</th><th>Quota Name</th><th>Max</th><th>Used</th><th>Remaining</th></>}
                          </tr>
                        </thead>
                        <tbody>
                          {subData.map((d) => (
                            <tr key={d.id}>
                              {resType === 'TIME_BASED' && <>
                                <td>#{d.id}</td>
                                <td>{d.startTime}</td>
                                <td>{d.endTime}</td>
                                <td>{d.maxParallelCapacity}</td>
                                <td>{d.currentBookings}</td>
                              </>}
                              {resType === 'SEAT_BASED' && <>
                                <td>#{d.id}</td>
                                <td><strong>{d.seatIdentifier}</strong></td>
                                <td>{d.seatRow}</td>
                                <td>{d.seatColumn}</td>
                                <td><span className={`badge ${d.isAvailable ? 'badge-active' : 'badge-cancelled'}`}>{d.isAvailable ? 'Available' : 'Taken'}</span></td>
                              </>}
                              {resType === 'QUOTA_BASED' && <>
                                <td>#{d.id}</td>
                                <td><strong>{d.quotaName}</strong></td>
                                <td>{d.maxAllocation}</td>
                                <td>{d.currentUsage}</td>
                                <td>{d.maxAllocation - d.currentUsage}</td>
                              </>}
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                </>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Modal: Create Resource Type */}
      <Modal isOpen={modal === 'type'} onClose={() => setModal(null)} title="Create Resource Type">
        <form onSubmit={submitResourceType}>
          <div className="modal__body">
            <div className="form-row cols-1">
              <div className="form-group">
                <label className="form-label">Name <span>*</span></label>
                <input className="form-control" placeholder="e.g. Salon, Movie Theatre" value={rtForm.name}
                  onChange={(e) => setRtForm({ ...rtForm, name: e.target.value })} required />
              </div>
              <div className="form-group">
                <label className="form-label">Reservation Type <span>*</span></label>
                <select className="form-control" value={rtForm.reservationType}
                  onChange={(e) => setRtForm({ ...rtForm, reservationType: e.target.value })}>
                  {TYPES.map((t) => <option key={t} value={t}>{typeIcon(t)} {typeLabel(t)}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Description</label>
                <input className="form-control" placeholder="Brief description" value={rtForm.description}
                  onChange={(e) => setRtForm({ ...rtForm, description: e.target.value })} />
              </div>
            </div>
          </div>
          <div className="modal__footer">
            <button type="button" className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
            <button type="submit" className="btn btn-primary">Create Type</button>
          </div>
        </form>
      </Modal>

      {/* Modal: Create Resource */}
      <Modal isOpen={modal === 'resource'} onClose={() => setModal(null)} title="Create Resource">
        <form onSubmit={submitResource}>
          <div className="modal__body">
            <div className="form-row cols-1">
              <div className="form-group">
                <label className="form-label">Resource Type <span>*</span></label>
                <select className="form-control" value={rForm.resourceTypeId}
                  onChange={(e) => setRForm({ ...rForm, resourceTypeId: e.target.value })} required>
                  <option value="">Select a type…</option>
                  {resourceTypes.map((rt) => <option key={rt.id} value={rt.id}>{rt.name} ({typeLabel(rt.reservationType)})</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Resource Name <span>*</span></label>
                <input className="form-control" placeholder="e.g. GlamUp Salon - Room A" value={rForm.name}
                  onChange={(e) => setRForm({ ...rForm, name: e.target.value })} required />
              </div>
              {resourceTypes.find(rt => rt.id === Number(rForm.resourceTypeId))?.reservationType === 'CAPACITY_BASED' && (
                <div className="form-group">
                  <label className="form-label">Total Capacity <span>*</span></label>
                  <input type="number" min={1} className="form-control" placeholder="e.g. 100" value={rForm.totalCapacity}
                    onChange={(e) => setRForm({ ...rForm, totalCapacity: e.target.value })} required />
                </div>
              )}
              <div className="form-group">
                <label className="form-label">Description</label>
                <input className="form-control" placeholder="Optional description" value={rForm.description}
                  onChange={(e) => setRForm({ ...rForm, description: e.target.value })} />
              </div>
            </div>
          </div>
          <div className="modal__footer">
            <button type="button" className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
            <button type="submit" className="btn btn-primary">Create Resource</button>
          </div>
        </form>
      </Modal>

      {/* Modal: Add Time Slot */}
      <Modal isOpen={modal === 'slot'} onClose={() => setModal(null)} title={`Add Time Slot — ${selectedResource?.name}`}>
        <form onSubmit={submitSlot}>
          <div className="modal__body">
            <div className="form-row cols-2">
              <div className="form-group">
                <label className="form-label">Start Time <span>*</span></label>
                <input type="time" className="form-control" value={slotForm.startTime}
                  onChange={(e) => setSlotForm({ ...slotForm, startTime: e.target.value })} required />
              </div>
              <div className="form-group">
                <label className="form-label">End Time <span>*</span></label>
                <input type="time" className="form-control" value={slotForm.endTime}
                  onChange={(e) => setSlotForm({ ...slotForm, endTime: e.target.value })} required />
              </div>
            </div>
            <div className="form-group">
              <label className="form-label">Max Parallel Capacity</label>
              <input type="number" min={1} className="form-control" value={slotForm.maxParallelCapacity}
                onChange={(e) => setSlotForm({ ...slotForm, maxParallelCapacity: e.target.value })} />
              <span className="form-hint">Number of simultaneous bookings allowed in this slot</span>
            </div>
          </div>
          <div className="modal__footer">
            <button type="button" className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
            <button type="submit" className="btn btn-primary">Add Slot</button>
          </div>
        </form>
      </Modal>

      {/* Modal: Add Seat */}
      <Modal isOpen={modal === 'seat'} onClose={() => setModal(null)} title={`Add Seat — ${selectedResource?.name}`}>
        <form onSubmit={submitSeat}>
          <div className="modal__body">
            <div className="form-row cols-3">
              <div className="form-group">
                <label className="form-label">Seat ID <span>*</span></label>
                <input className="form-control" placeholder="A1" value={seatForm.seatIdentifier}
                  onChange={(e) => setSeatForm({ ...seatForm, seatIdentifier: e.target.value })} required />
              </div>
              <div className="form-group">
                <label className="form-label">Row <span>*</span></label>
                <input className="form-control" placeholder="A" value={seatForm.seatRow}
                  onChange={(e) => setSeatForm({ ...seatForm, seatRow: e.target.value })} required />
              </div>
              <div className="form-group">
                <label className="form-label">Column <span>*</span></label>
                <input type="number" min={1} className="form-control" placeholder="1" value={seatForm.seatColumn}
                  onChange={(e) => setSeatForm({ ...seatForm, seatColumn: e.target.value })} required />
              </div>
            </div>
          </div>
          <div className="modal__footer">
            <button type="button" className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
            <button type="submit" className="btn btn-primary">Add Seat</button>
          </div>
        </form>
      </Modal>

      {/* Modal: Add Quota */}
      <Modal isOpen={modal === 'quota'} onClose={() => setModal(null)} title={`Add Quota — ${selectedResource?.name}`}>
        <form onSubmit={submitQuota}>
          <div className="modal__body">
            <div className="form-row cols-2">
              <div className="form-group">
                <label className="form-label">Quota Name <span>*</span></label>
                <input className="form-control" placeholder="General / Premium / VIP" value={quotaForm.quotaName}
                  onChange={(e) => setQuotaForm({ ...quotaForm, quotaName: e.target.value })} required />
              </div>
              <div className="form-group">
                <label className="form-label">Max Allocation <span>*</span></label>
                <input type="number" min={1} className="form-control" placeholder="50" value={quotaForm.maxAllocation}
                  onChange={(e) => setQuotaForm({ ...quotaForm, maxAllocation: e.target.value })} required />
              </div>
            </div>
          </div>
          <div className="modal__footer">
            <button type="button" className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
            <button type="submit" className="btn btn-primary">Add Quota</button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
